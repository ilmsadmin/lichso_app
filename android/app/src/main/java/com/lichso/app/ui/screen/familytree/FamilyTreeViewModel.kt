package com.lichso.app.ui.screen.familytree

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.FamilyTreeExportImport
import com.lichso.app.data.local.FamilyTreeRepository
import com.lichso.app.data.local.dao.NoteDao
import com.lichso.app.data.local.dao.ReminderDao
import com.lichso.app.data.local.entity.FamilySettingsEntity
import com.lichso.app.data.local.entity.MemorialChecklistEntity
import com.lichso.app.data.local.entity.MemberPhotoEntity
import com.lichso.app.data.local.entity.NoteEntity
import com.lichso.app.data.local.entity.ReminderEntity
import com.lichso.app.util.CanChiCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

// ══════════════════════════════════════════════════════════════
// DATA MODELS
// ══════════════════════════════════════════════════════════════

enum class Gender { MALE, FEMALE }

data class FamilyMember(
    val id: String,
    val name: String,
    val role: String,
    val gender: Gender,
    val generation: Int,
    val birthYear: Int?,
    val deathYear: Int? = null,
    val birthDateLunar: String? = null,
    val deathDateLunar: String? = null,
    val canChi: String? = null,
    val menh: String? = null,
    val zodiacEmoji: String? = null,
    val menhEmoji: String? = null,
    val hanhEmoji: String? = null,
    val menhDetail: String? = null,
    val zodiacName: String? = null,
    val menhName: String? = null,
    val hometown: String? = null,
    val occupation: String? = null,
    val isSelf: Boolean = false,
    val isElder: Boolean = false,
    val emoji: String = "👤",
    val spouseId: String? = null,
    val parentIds: List<String> = emptyList(),
    val note: String? = null,
    val avatarPath: String? = null,
)

data class MemorialDay(
    val id: String,
    val memberId: String,
    val memberName: String,
    val relation: String,
    val lunarDay: Int,
    val lunarMonth: Int,
    val lunarLeap: Int = 0,
    val lunarDate: String,          // computed: "20/11 Âm lịch"
    val solarDate: String,          // computed: "30/12/2026"
    val countdown: String,          // computed: "Còn 269 ngày nữa"
    val countdownDays: Long = 0,    // computed: number of days
    val isUpcoming: Boolean = false, // computed: within 30 days
    val yearCanChi: String = "",     // computed: "Bính Ngọ"
    val note: String? = null,
    val remindBefore3Days: Boolean = true,
    val remindBefore1Day: Boolean = true,
    val remind3DaysDateStr: String = "",  // computed
    val remind1DayDateStr: String = "",   // computed
)

data class Relationship(
    val memberId: String,
    val name: String,
    val role: String,
    val emoji: String,
    val gender: Gender,
    val isElder: Boolean = false,
)

data class MemberPhoto(
    val id: Long,
    val memberId: String,
    val filePath: String,
    val caption: String? = null,
    val createdAt: Long = 0,
)

data class TreeLevel(
    val generation: Int,
    val label: String,
    val nodes: List<TreeNode>,
)

sealed class TreeNode {
    data class Couple(val person1: FamilyMember, val person2: FamilyMember) : TreeNode()
    data class Single(val person: FamilyMember) : TreeNode()
    data object AddPlaceholder : TreeNode()
}

/**
 * A family group represents a parent couple/single and their direct children.
 * This is used to build a proper hierarchical tree where children are
 * visually grouped under their parents.
 */
data class FamilyGroup(
    val parents: TreeNode,                // Couple or Single parent
    val children: List<FamilyGroup>,      // child groups (each child may have their own family)
    val generation: Int,
)

data class FamilyTreeUiState(
    val familyName: String = "Dòng họ Nguyễn",
    val familyCrest: String = "Ng",
    val familyHometown: String = "Hà Nam",
    val totalGenerations: Int = 0,
    val totalMembers: Int = 0,
    val selectedTab: Int = 0,
    val selectedMemberId: String? = null,
    val showMemberDetail: Boolean = false,
    val showAddMember: Boolean = false,
    val showEditMember: Boolean = false,
    val editMemberId: String? = null,
    val showFamilySettings: Boolean = false,
    val showPickMember: Boolean = false,
    val showMemorialDetail: Boolean = false,
    val selectedMemorialId: String? = null,
    val members: List<FamilyMember> = emptyList(),
    val memorials: List<MemorialDay> = emptyList(),
    val isLoading: Boolean = true,
    val pickMemberCallback: ((FamilyMember) -> Unit)? = null,
    // Memorial quick-add note/reminder
    val showMemorialNoteEdit: Boolean = false,
    val showMemorialReminderEdit: Boolean = false,
    val memorialEditTarget: MemorialDay? = null,
    // Member quick-add reminder (birthday / memorial)
    val showMemberReminderEdit: Boolean = false,
    val memberReminderTarget: FamilyMember? = null,
    // Export / Import
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportImportMessage: String? = null,
    val showImportConfirmDialog: Boolean = false,
    val pendingImportUri: Uri? = null,
)

// ══════════════════════════════════════════════════════════════
// VIEW MODEL
// ══════════════════════════════════════════════════════════════

@HiltViewModel
class FamilyTreeViewModel @Inject constructor(
    private val repository: FamilyTreeRepository,
    private val noteDao: NoteDao,
    private val reminderDao: ReminderDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyTreeUiState())
    val uiState: StateFlow<FamilyTreeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed database with initial data if empty — MUST complete before observing
            repository.seedIfEmpty()

            // Observe members
            launch {
                repository.getAllMembersFlow().collect { members ->
                    _uiState.update { it.copy(
                        members = members,
                        totalMembers = members.size,
                        totalGenerations = members.maxOfOrNull { m -> m.generation } ?: 0,
                        isLoading = false,
                    ) }
                }
            }

            // Observe memorials
            launch {
                repository.getAllMemorialsFlow().collect { memorials ->
                    _uiState.update { it.copy(memorials = memorials) }
                }
            }

            // Observe settings
            launch {
                repository.getSettingsFlow().collect { settings ->
                    settings?.let { s ->
                        _uiState.update { it.copy(
                            familyName = s.familyName,
                            familyCrest = s.familyCrest,
                            familyHometown = s.hometown,
                        ) }
                    }
                }
            }
        }
    }

    // ── Convenience getters ──

    val members: List<FamilyMember>
        get() = _uiState.value.members

    val memorials: List<MemorialDay>
        get() = _uiState.value.memorials

    fun getMember(id: String): FamilyMember? = members.find { it.id == id }

    fun getTreeLevels(): List<TreeLevel> {
        val memberList = members
        if (memberList.isEmpty()) return emptyList()

        val grouped = memberList.groupBy { it.generation }
        val generations = grouped.keys.sorted()

        return generations.map { gen ->
            val genMembers = grouped[gen] ?: emptyList()
            val nodes = mutableListOf<TreeNode>()
            val processed = mutableSetOf<String>()

            for (member in genMembers) {
                if (member.id in processed) continue
                val spouse = member.spouseId?.let { sid -> genMembers.find { it.id == sid } }
                if (spouse != null && spouse.id !in processed) {
                    nodes.add(TreeNode.Couple(member, spouse))
                    processed.add(member.id)
                    processed.add(spouse.id)
                } else {
                    nodes.add(TreeNode.Single(member))
                    processed.add(member.id)
                }
            }

            if (gen == generations.last()) {
                nodes.add(TreeNode.AddPlaceholder)
            }

            TreeLevel(gen, getGenerationLabel(gen), nodes)
        }
    }

    /**
     * Build a hierarchical family tree as a list of root FamilyGroups.
     * Each group has parents and their children recursively nested.
     * Children are grouped under their actual parents, not just by generation.
     */
    fun getFamilyTree(): List<FamilyGroup> {
        val memberList = members
        if (memberList.isEmpty()) return emptyList()

        val memberById = memberList.associateBy { it.id }
        val grouped = memberList.groupBy { it.generation }
        val generations = grouped.keys.sorted()
        if (generations.isEmpty()) return emptyList()

        // Find root generation members (first generation)
        val rootGen = generations.first()
        val rootMembers = grouped[rootGen] ?: emptyList()

        // Build parent nodes (couple or single) for roots
        val rootNodes = buildParentNodes(rootMembers, memberById)

        // Recursively build family groups
        return rootNodes.map { node ->
            buildFamilyGroup(node, rootGen, memberById, memberList)
        }
    }

    /**
     * Build parent nodes (Couple or Single) from a list of members,
     * handling spouse pairing — also pull in spouses from other generations
     * who married into this family.
     */
    private fun buildParentNodes(
        members: List<FamilyMember>,
        memberById: Map<String, FamilyMember>,
    ): List<TreeNode> {
        val nodes = mutableListOf<TreeNode>()
        val processed = mutableSetOf<String>()

        for (member in members) {
            if (member.id in processed) continue
            val spouse = member.spouseId?.let { sid ->
                members.find { it.id == sid } ?: memberById[sid]
            }
            if (spouse != null && spouse.id !in processed) {
                nodes.add(TreeNode.Couple(member, spouse))
                processed.add(member.id)
                processed.add(spouse.id)
            } else {
                nodes.add(TreeNode.Single(member))
                processed.add(member.id)
            }
        }
        return nodes
    }

    /**
     * Get all member IDs that belong to a TreeNode (parent unit).
     */
    private fun getNodeMemberIds(node: TreeNode): Set<String> = when (node) {
        is TreeNode.Couple -> setOf(node.person1.id, node.person2.id)
        is TreeNode.Single -> setOf(node.person.id)
        is TreeNode.AddPlaceholder -> emptySet()
    }

    /**
     * Find children of a parent node from the full member list.
     * A child belongs to this parent if any of its parentIds match the node's member IDs.
     */
    private fun findChildrenOf(
        parentNode: TreeNode,
        allMembers: List<FamilyMember>,
    ): List<FamilyMember> {
        val parentIds = getNodeMemberIds(parentNode)
        if (parentIds.isEmpty()) return emptyList()
        return allMembers.filter { member ->
            member.parentIds.any { it in parentIds }
        }
    }

    /**
     * Recursively build a FamilyGroup: a parent node with its children,
     * where each child may form their own family group.
     */
    private fun buildFamilyGroup(
        parentNode: TreeNode,
        generation: Int,
        memberById: Map<String, FamilyMember>,
        allMembers: List<FamilyMember>,
    ): FamilyGroup {
        // Find direct children of this parent unit
        val children = findChildrenOf(parentNode, allMembers)

        // Group children into parent nodes (couple or single) for the next generation
        val childParentNodes = buildParentNodes(children, memberById)

        // Recursively build children's family groups
        val childGroups = childParentNodes.map { childNode ->
            buildFamilyGroup(childNode, generation + 1, memberById, allMembers)
        }

        return FamilyGroup(
            parents = parentNode,
            children = childGroups,
            generation = generation,
        )
    }

    fun getMembersByGeneration(): Map<Int, List<FamilyMember>> =
        members.groupBy { it.generation }

    fun getGenerationLabel(gen: Int): String = when (gen) {
        1 -> "Đời 1 — Ông Bà Cố"
        2 -> "Đời 2 — Ông Bà"
        3 -> "Đời 3 — Bố Mẹ"
        4 -> "Đời 4 — Bản thân"
        else -> "Đời $gen"
    }

    fun getRelationshipsFor(memberId: String): List<Relationship> {
        val member = getMember(memberId) ?: return emptyList()
        val result = mutableListOf<Relationship>()

        member.parentIds.forEach { pid ->
            val p = getMember(pid)
            if (p != null) {
                val relLabel = if (p.gender == Gender.MALE) "Cha (${p.role})" else "Mẹ (${p.role})"
                result.add(Relationship(p.id, p.name, relLabel, p.emoji, p.gender, p.isElder))
            }
        }

        member.spouseId?.let { sid ->
            val s = getMember(sid)
            if (s != null) {
                val relLabel = if (s.gender == Gender.MALE) "Chồng (${s.role})" else "Vợ (${s.role})"
                result.add(Relationship(s.id, s.name, relLabel, s.emoji, s.gender, s.isElder))
            }
        }

        members.filter { it.parentIds.contains(memberId) }.forEach { child ->
            val relLabel = if (child.gender == Gender.MALE) "Con trai (${child.role})" else "Con gái (${child.role})"
            result.add(Relationship(child.id, child.name, relLabel, child.emoji, child.gender, child.isElder))
        }

        if (member.parentIds.isNotEmpty()) {
            members.filter {
                it.id != memberId && it.parentIds.any { pid -> member.parentIds.contains(pid) }
            }.forEach { sib ->
                val relLabel = if (sib.gender == Gender.MALE) "Anh/Em trai" else "Chị/Em gái"
                result.add(Relationship(sib.id, sib.name, relLabel, sib.emoji, sib.gender, sib.isElder))
            }
        }

        return result
    }

    fun getMemorialForMember(memberId: String): MemorialDay? =
        memorials.find { it.memberId == memberId }

    // ── Tab / Navigation actions ──

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun openMemberDetail(memberId: String) {
        _uiState.update { it.copy(selectedMemberId = memberId, showMemberDetail = true) }
    }

    fun closeMemberDetail() {
        _uiState.update { it.copy(showMemberDetail = false) }
    }

    fun openAddMember() {
        _uiState.update { it.copy(showAddMember = true) }
    }

    fun closeAddMember() {
        _uiState.update { it.copy(showAddMember = false) }
    }

    fun openEditMember(memberId: String) {
        _uiState.update { it.copy(showEditMember = true, editMemberId = memberId) }
    }

    fun closeEditMember() {
        _uiState.update { it.copy(showEditMember = false, editMemberId = null) }
    }

    fun openFamilySettings() {
        _uiState.update { it.copy(showFamilySettings = true) }
    }

    fun closeFamilySettings() {
        _uiState.update { it.copy(showFamilySettings = false) }
    }

    fun openPickMember(callback: (FamilyMember) -> Unit) {
        _uiState.update { it.copy(showPickMember = true, pickMemberCallback = callback) }
    }

    fun closePickMember() {
        _uiState.update { it.copy(showPickMember = false, pickMemberCallback = null) }
    }

    fun onMemberPicked(member: FamilyMember) {
        _uiState.value.pickMemberCallback?.invoke(member)
        closePickMember()
    }

    fun openMemorialDetail(memorialId: String) {
        _uiState.update { it.copy(showMemorialDetail = true, selectedMemorialId = memorialId) }
    }

    fun closeMemorialDetail() {
        _uiState.update { it.copy(showMemorialDetail = false, selectedMemorialId = null) }
    }

    // ── CRUD Operations ──

    fun saveMember(member: FamilyMember) {
        viewModelScope.launch {
            val existing = repository.getMemberById(member.id)
            if (existing != null) {
                repository.updateMember(member)
            } else {
                repository.addMember(member)
            }

            // Auto-sync memorial day based on death date
            syncMemorialForMember(member)

            closeAddMember()
            closeEditMember()
        }
    }

    /**
     * Automatically sync memorial day for a member:
     * - Has deathDateLunar → create or update memorial
     * - No deathDateLunar / not deceased → delete memorial if exists
     */
    private suspend fun syncMemorialForMember(member: FamilyMember) {
        val deathLunar = member.deathDateLunar
        val parts = deathLunar?.split("/")
        val lunarDay = parts?.getOrNull(0)?.toIntOrNull()
        val lunarMonth = parts?.getOrNull(1)?.toIntOrNull()

        if (member.deathYear != null && lunarDay != null && lunarMonth != null
            && lunarDay in 1..30 && lunarMonth in 1..12
        ) {
            // Deceased with valid lunar date → create or update memorial
            val existingMemorial = repository.getMemorialByMemberId(member.id)
            if (existingMemorial != null) {
                // Update date & name if changed
                repository.updateMemorial(existingMemorial.copy(
                    memberName = "Giỗ ${member.role} ${member.name}",
                    relation = "${member.role} · Đời ${member.generation}",
                    lunarDay = lunarDay,
                    lunarMonth = lunarMonth,
                ))
            } else {
                // Create new memorial
                val relation = "${member.role} · Đời ${member.generation}"
                val memorial = MemorialDay(
                    id = FamilyTreeRepository.generateId(),
                    memberId = member.id,
                    memberName = "Giỗ ${member.role} ${member.name}",
                    relation = relation,
                    lunarDay = lunarDay,
                    lunarMonth = lunarMonth,
                    lunarDate = "", solarDate = "", countdown = "",
                )
                repository.addMemorial(memorial)
            }
        } else {
            // Not deceased or no valid lunar date → remove memorial if exists
            repository.deleteMemorialByMemberId(member.id)
        }
    }

    fun deleteMember(memberId: String) {
        viewModelScope.launch {
            // Clean up memorial when deleting member
            repository.deleteMemorialByMemberId(memberId)
            repository.deleteMember(memberId)
            closeMemberDetail()
        }
    }

    fun saveMemorial(memorial: MemorialDay) {
        viewModelScope.launch {
            val existing = repository.getMemorialById(memorial.id)
            if (existing != null) {
                repository.updateMemorial(memorial)
            } else {
                repository.addMemorial(memorial)
            }
        }
    }

    fun deleteMemorial(memorialId: String) {
        viewModelScope.launch {
            // Clear the member's death lunar date so it won't be re-created
            val memorial = repository.getMemorialById(memorialId)
            if (memorial != null) {
                val member = repository.getMemberById(memorial.memberId)
                if (member != null) {
                    repository.updateMember(member.copy(deathDateLunar = null))
                }
            }
            repository.deleteMemorial(memorialId)
            closeMemorialDetail()
        }
    }

    fun updateMemorialRemind3Days(memorialId: String, enabled: Boolean) {
        viewModelScope.launch {
            val memorial = repository.getMemorialById(memorialId) ?: return@launch
            repository.updateMemorial(memorial.copy(remindBefore3Days = enabled))
        }
    }

    fun updateMemorialRemind1Day(memorialId: String, enabled: Boolean) {
        viewModelScope.launch {
            val memorial = repository.getMemorialById(memorialId) ?: return@launch
            repository.updateMemorial(memorial.copy(remindBefore1Day = enabled))
        }
    }

    fun updateMemorialNote(memorialId: String, note: String?) {
        viewModelScope.launch {
            val memorial = repository.getMemorialById(memorialId) ?: return@launch
            repository.updateMemorial(memorial.copy(note = note))
        }
    }

    fun updateMemorialDate(memorialId: String, lunarDay: Int, lunarMonth: Int) {
        viewModelScope.launch {
            val memorial = repository.getMemorialById(memorialId) ?: return@launch
            // Update the memorial directly
            repository.updateMemorial(memorial.copy(
                lunarDay = lunarDay,
                lunarMonth = lunarMonth,
            ))
            // Also update the member's deathDateLunar to keep in sync
            val member = repository.getMemberById(memorial.memberId) ?: return@launch
            val oldParts = member.deathDateLunar?.split("/")
            val yearPart = oldParts?.getOrNull(2) ?: ""
            val newDeathLunar = if (yearPart.isNotBlank()) "$lunarDay/$lunarMonth/$yearPart"
                                else "$lunarDay/$lunarMonth"
            repository.updateMember(member.copy(deathDateLunar = newDeathLunar))
        }
    }

    // ── Memorial quick-add Note / Reminder ──

    fun openMemorialNoteEdit(memorial: MemorialDay) {
        _uiState.update { it.copy(showMemorialNoteEdit = true, memorialEditTarget = memorial) }
    }

    fun openMemorialReminderEdit(memorial: MemorialDay) {
        _uiState.update { it.copy(showMemorialReminderEdit = true, memorialEditTarget = memorial) }
    }

    fun closeMemorialEdit() {
        _uiState.update { it.copy(showMemorialNoteEdit = false, showMemorialReminderEdit = false, memorialEditTarget = null) }
    }

    fun saveNoteForMemorial(note: NoteEntity) {
        viewModelScope.launch { noteDao.insert(note) }
    }

    fun saveReminderForMemorial(reminder: ReminderEntity) {
        viewModelScope.launch { reminderDao.insert(reminder) }
    }

    // ── Member quick-add Reminder (birthday / memorial from list) ──

    fun openMemberReminderEdit(member: FamilyMember) {
        _uiState.update { it.copy(showMemberReminderEdit = true, memberReminderTarget = member) }
    }

    fun closeMemberReminderEdit() {
        _uiState.update { it.copy(showMemberReminderEdit = false, memberReminderTarget = null) }
    }

    fun saveMemberReminder(reminder: ReminderEntity) {
        viewModelScope.launch { reminderDao.insert(reminder) }
    }

    // ── Settings ──

    fun updateFamilySettings(
        familyName: String? = null,
        familyCrest: String? = null,
        hometown: String? = null,
        remindMemorial: Boolean? = null,
        remindBirthday: Boolean? = null,
        remindDaysBefore: Int? = null,
        showAvatar: Boolean? = null,
        showYears: Boolean? = null,
    ) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(
                current.copy(
                    familyName = familyName ?: current.familyName,
                    familyCrest = familyCrest ?: current.familyCrest,
                    hometown = hometown ?: current.hometown,
                    remindMemorial = remindMemorial ?: current.remindMemorial,
                    remindBirthday = remindBirthday ?: current.remindBirthday,
                    remindDaysBefore = remindDaysBefore ?: current.remindDaysBefore,
                    showAvatar = showAvatar ?: current.showAvatar,
                    showYears = showYears ?: current.showYears,
                )
            )
        }
    }

    fun deleteAllFamilyData() {
        viewModelScope.launch {
            repository.deleteAllMembers()
        }
    }

    // ── Export / Import ──

    /**
     * Build the export JSON from all family data.
     * Returns the JSON string or null on failure.
     */
    fun exportFamilyData(onJsonReady: (String, String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportImportMessage = null) }
            try {
                val rawData = repository.getAllDataForExport()
                val json = FamilyTreeExportImport.buildExportJson(
                    settings = rawData.settings,
                    members = rawData.members,
                    memorials = rawData.memorials,
                    checklistItems = rawData.checklistItems,
                    photos = rawData.photos,
                )
                val fileName = FamilyTreeExportImport.generateFileName(
                    rawData.settings?.familyName ?: "GiaPha"
                )
                onJsonReady(json, fileName)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isExporting = false,
                    exportImportMessage = "Lỗi xuất dữ liệu: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Write exported JSON to a SAF Uri (called after user picks save location).
     */
    fun writeExportToUri(uri: Uri, json: String) {
        viewModelScope.launch {
            val success = FamilyTreeExportImport.writeToUri(context, uri, json)
            _uiState.update { it.copy(
                isExporting = false,
                exportImportMessage = if (success) "Xuất gia phả thành công! ✅"
                    else "Lỗi khi ghi file xuất"
            ) }
        }
    }

    /**
     * Show import confirmation dialog before processing.
     */
    fun requestImport(uri: Uri) {
        _uiState.update { it.copy(showImportConfirmDialog = true, pendingImportUri = uri) }
    }

    fun cancelImport() {
        _uiState.update { it.copy(showImportConfirmDialog = false, pendingImportUri = null) }
    }

    /**
     * Import family data from a JSON file URI.
     * Replaces ALL existing family data.
     */
    fun confirmImport() {
        val uri = _uiState.value.pendingImportUri ?: return
        _uiState.update { it.copy(showImportConfirmDialog = false, isImporting = true, exportImportMessage = null) }

        viewModelScope.launch {
            try {
                val jsonStr = FamilyTreeExportImport.readFromUri(context, uri)
                if (jsonStr.isNullOrBlank()) {
                    _uiState.update { it.copy(isImporting = false, exportImportMessage = "Không đọc được file") }
                    return@launch
                }

                val exportData = FamilyTreeExportImport.parseExportJson(jsonStr)
                if (exportData == null) {
                    _uiState.update { it.copy(isImporting = false, exportImportMessage = "File không hợp lệ hoặc không phải dữ liệu Lịch Số") }
                    return@launch
                }

                // Delete existing photo files before replacing
                repository.deleteAllPhotoFiles(context)

                // Convert to entities (also restores photo files)
                val entities = FamilyTreeExportImport.convertToEntities(context, exportData)

                // Insert into database
                repository.importAllData(entities)

                _uiState.update { it.copy(
                    isImporting = false,
                    pendingImportUri = null,
                    exportImportMessage = "Nhập gia phả thành công! ✅\n" +
                        "${entities.members.size} thành viên, ${entities.memorials.size} ngày giỗ, " +
                        "${entities.photos.size} ảnh"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isImporting = false,
                    pendingImportUri = null,
                    exportImportMessage = "Lỗi nhập dữ liệu: ${e.message}"
                ) }
            }
        }
    }

    fun clearExportImportMessage() {
        _uiState.update { it.copy(exportImportMessage = null) }
    }

    // ── Checklist ──

    fun getChecklistFlow(memorialId: String) = repository.getChecklistFlow(memorialId)

    fun addChecklistItem(memorialId: String, text: String) {
        viewModelScope.launch {
            repository.addChecklistItem(memorialId, text)
        }
    }

    fun toggleChecklistItem(id: Long, isDone: Boolean) {
        viewModelScope.launch {
            repository.toggleChecklistItem(id, isDone)
        }
    }

    fun deleteChecklistItem(item: MemorialChecklistEntity) {
        viewModelScope.launch {
            repository.deleteChecklistItem(item)
        }
    }

    // ── Avatar ──

    /**
     * Copy ảnh từ gallery URI vào internal storage rồi trả về path.
     * Mỗi member có file riêng: avatars/member_{id}.jpg
     */
    fun saveAvatarFromUri(uri: Uri, memberId: String, callback: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val savedPath = withContext(Dispatchers.IO) {
                    val avatarDir = File(context.filesDir, "avatars")
                    if (!avatarDir.exists()) avatarDir.mkdirs()

                    // Use timestamp in filename to ensure Coil doesn't serve stale cache
                    val ts = System.currentTimeMillis()
                    val destFile = File(avatarDir, "member_${memberId}_$ts.jpg")

                    // Delete previous avatar files for this member
                    avatarDir.listFiles()?.filter {
                        it.name.startsWith("member_${memberId}") && it != destFile
                    }?.forEach { it.delete() }

                    // Decode bitmap with down-sampling & fix EXIF rotation
                    val bitmap = decodeSampledBitmap(context, uri, 512, 512)
                        ?: return@withContext null

                    val rotatedBitmap = context.contentResolver.openInputStream(uri)?.use { exifStream ->
                        val exif = ExifInterface(exifStream)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        rotateBitmap(bitmap, orientation)
                    } ?: bitmap

                    destFile.outputStream().use { output ->
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                    }
                    if (rotatedBitmap !== bitmap) rotatedBitmap.recycle()
                    bitmap.recycle()

                    destFile.absolutePath
                }
                if (savedPath != null) callback(savedPath)
            } catch (_: Exception) { }
        }
    }

    fun removeAvatar(memberId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val avatarDir = File(context.filesDir, "avatars")
                avatarDir.listFiles()?.filter {
                    it.name.startsWith("member_${memberId}")
                }?.forEach { it.delete() }
            }
        }
    }

    // ── Member Photos ──

    fun getPhotosFlow(memberId: String): Flow<List<MemberPhoto>> =
        repository.getPhotosForMemberFlow(memberId).map { list ->
            list.map { MemberPhoto(it.id, it.memberId, it.filePath, it.caption, it.createdAt) }
        }

    fun addMemberPhoto(uri: Uri, memberId: String) {
        viewModelScope.launch {
            try {
                val savedPath = withContext(Dispatchers.IO) {
                    val photoDir = File(context.filesDir, "member_photos/$memberId")
                    if (!photoDir.exists()) photoDir.mkdirs()

                    val ts = System.currentTimeMillis()
                    val destFile = File(photoDir, "photo_${ts}.jpg")

                    // Decode bitmap with down-sampling to avoid OOM
                    val bitmap = decodeSampledBitmap(context, uri, 1600, 1600)
                        ?: return@withContext null

                    // Read EXIF orientation and rotate if needed
                    val rotatedBitmap = context.contentResolver.openInputStream(uri)?.use { exifStream ->
                        val exif = ExifInterface(exifStream)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        rotateBitmap(bitmap, orientation)
                    } ?: bitmap

                    // Save as JPEG
                    destFile.outputStream().use { output ->
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                    }
                    if (rotatedBitmap !== bitmap) rotatedBitmap.recycle()
                    bitmap.recycle()

                    destFile.absolutePath
                }
                if (savedPath != null) {
                    repository.addPhoto(memberId, savedPath)
                }
            } catch (_: Exception) { }
        }
    }

    fun addMemberPhotos(uris: List<Uri>, memberId: String) {
        uris.forEach { uri -> addMemberPhoto(uri, memberId) }
    }

    fun deleteMemberPhoto(photo: MemberPhoto) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(photo.filePath)
                if (file.exists()) file.delete()
            }
            repository.deletePhoto(photo.id)
        }
    }

    // ── Can Chi calculation helpers ──

    private val napAmList = listOf(
        "Hải Trung Kim", "Hải Trung Kim",
        "Lư Trung Hỏa", "Lư Trung Hỏa",
        "Đại Lâm Mộc", "Đại Lâm Mộc",
        "Lộ Bàng Thổ", "Lộ Bàng Thổ",
        "Kiếm Phong Kim", "Kiếm Phong Kim",
        "Sơn Đầu Hỏa", "Sơn Đầu Hỏa",
        "Giản Hạ Thủy", "Giản Hạ Thủy",
        "Thành Đầu Thổ", "Thành Đầu Thổ",
        "Bạch Lạp Kim", "Bạch Lạp Kim",
        "Dương Liễu Mộc", "Dương Liễu Mộc",
        "Tuyền Trung Thủy", "Tuyền Trung Thủy",
        "Ốc Thượng Thổ", "Ốc Thượng Thổ",
        "Tích Lịch Hỏa", "Tích Lịch Hỏa",
        "Tùng Bách Mộc", "Tùng Bách Mộc",
        "Trường Lưu Thủy", "Trường Lưu Thủy",
        "Sa Trung Kim", "Sa Trung Kim",
        "Sơn Hạ Hỏa", "Sơn Hạ Hỏa",
        "Bình Địa Mộc", "Bình Địa Mộc",
        "Bích Thượng Thổ", "Bích Thượng Thổ",
        "Kim Bạch Kim", "Kim Bạch Kim",
        "Phúc Đăng Hỏa", "Phúc Đăng Hỏa",
        "Thiên Hà Thủy", "Thiên Hà Thủy",
        "Đại Dịch Thổ", "Đại Dịch Thổ",
        "Thoa Xuyến Kim", "Thoa Xuyến Kim",
        "Tang Đố Mộc", "Tang Đố Mộc",
        "Đại Khê Thủy", "Đại Khê Thủy",
        "Sa Trung Thổ", "Sa Trung Thổ",
        "Thiên Thượng Hỏa", "Thiên Thượng Hỏa",
        "Thạch Lựu Mộc", "Thạch Lựu Mộc",
        "Đại Hải Thủy", "Đại Hải Thủy"
    )

    private val conGiapNames = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
    private val conGiapAnimals = listOf("Chuột", "Trâu", "Hổ", "Mèo", "Rồng", "Rắn", "Ngựa", "Dê", "Khỉ", "Gà", "Chó", "Lợn")
    private val conGiapEmojis = listOf("🐭", "🐮", "🐯", "🐱", "🐲", "🐍", "🐴", "🐐", "🐒", "🐔", "🐶", "🐷")

    data class CanChiInfo(
        val yearCanChi: String,
        val napAm: String,
        val menh: String,
        val menhEmoji: String,
        val zodiacName: String,
        val zodiacEmoji: String,
        val hanhName: String,
        val hanhEmoji: String,
    )

    fun calculateCanChi(lunarYear: Int): CanChiInfo {
        val yearCanChi = CanChiCalculator.getYearCanChi(lunarYear)
        val chiIndex = (lunarYear + 8) % 12
        val napAmIndex = ((lunarYear - 4) % 60 + 60) % 60
        val napAm = if (napAmIndex in napAmList.indices) napAmList[napAmIndex] else "Không rõ"

        val menh = when {
            napAm.contains("Kim") -> "Kim"
            napAm.contains("Mộc") -> "Mộc"
            napAm.contains("Thủy") -> "Thủy"
            napAm.contains("Hỏa") -> "Hỏa"
            napAm.contains("Thổ") -> "Thổ"
            else -> "Không rõ"
        }
        val menhEmoji = when (menh) {
            "Kim" -> "🥇"
            "Mộc" -> "🌳"
            "Thủy" -> "🌊"
            "Hỏa" -> "🔥"
            "Thổ" -> "🏔️"
            else -> "❓"
        }
        // Hành from Thiên Can
        val canIndex = (lunarYear + 6) % 10
        val hanh = when (canIndex) {
            0, 1 -> "Mộc"
            2, 3 -> "Hỏa"
            4, 5 -> "Thổ"
            6, 7 -> "Kim"
            8, 9 -> "Thủy"
            else -> ""
        }
        val hanhEmoji = when (hanh) {
            "Mộc" -> "🌳"
            "Hỏa" -> "🔥"
            "Thổ" -> "🏔️"
            "Kim" -> "🥇"
            "Thủy" -> "🌊"
            else -> "❓"
        }

        return CanChiInfo(
            yearCanChi = yearCanChi,
            napAm = napAm,
            menh = menh,
            menhEmoji = menhEmoji,
            zodiacName = "Năm ${conGiapAnimals[chiIndex]}",
            zodiacEmoji = conGiapEmojis[chiIndex],
            hanhName = "Hành $hanh",
            hanhEmoji = hanhEmoji,
        )
    }
}

// ══════════════════════════════════════════════════════════════
// Image utility helpers (outside the class)
// ══════════════════════════════════════════════════════════════

/**
 * Decode a bitmap from a content URI with sub-sampling so large images
 * don't cause OutOfMemoryError or rendering artefacts.
 */
private fun decodeSampledBitmap(
    context: Context,
    uri: Uri,
    reqWidth: Int,
    reqHeight: Int,
): Bitmap? {
    // First pass: read dimensions only
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false

    // Second pass: decode actual bitmap
    return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

/**
 * Rotate a bitmap according to its EXIF orientation tag so the
 * saved image always appears upright.
 */
private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f); matrix.preScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(270f); matrix.preScale(-1f, 1f)
        }
        else -> return bitmap // ORIENTATION_NORMAL or ORIENTATION_UNDEFINED
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
