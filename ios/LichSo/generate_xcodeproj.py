#!/usr/bin/env python3
"""
Generate LichSo.xcodeproj/project.pbxproj
"""
import os, hashlib, random

# ── UUID generator (24-char hex, Xcode style) ──
_counter = [0]
def uid():
    _counter[0] += 1
    raw = hashlib.md5(f"lichso-{_counter[0]}-{random.randint(0,999999)}".encode()).hexdigest()[:24]
    return raw.upper()

# ── Configuration ──
PROJECT_NAME = "LichSo"
BUNDLE_ID = "com.lichso.app"
DEPLOY_TARGET = "17.0"
TEAM_ID = ""  # Fill in your team ID if needed
SWIFT_VERSION = "5.0"

# ── Collect all .swift files ──
SRC_ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), PROJECT_NAME)
swift_files = []
for root, dirs, files in os.walk(SRC_ROOT):
    for f in files:
        if f.endswith(".swift"):
            rel = os.path.relpath(os.path.join(root, f), SRC_ROOT)
            swift_files.append(rel)
swift_files.sort()

# Assets catalog
ASSETS_CATALOG = "Assets.xcassets"
PREVIEW_ASSETS = "Preview Content/Preview Assets.xcassets"

# ── Assign UUIDs ──
# Project-level
proj_uid = uid()
root_group_uid = uid()
main_group_uid = uid()      # LichSo group
products_group_uid = uid()
frameworks_group_uid = uid()

# Target
target_uid = uid()
product_ref_uid = uid()

# Build phases
sources_phase_uid = uid()
frameworks_phase_uid = uid()
resources_phase_uid = uid()

# Build configurations
proj_debug_config_uid = uid()
proj_release_config_uid = uid()
proj_config_list_uid = uid()
target_debug_config_uid = uid()
target_release_config_uid = uid()
target_config_list_uid = uid()

# Assets
assets_ref_uid = uid()
assets_variant_group = uid()

# Preview
preview_group_uid = uid()
preview_assets_ref_uid = uid()

# File references and build files for .swift
file_ref_uids = {}
build_file_uids = {}
for sf in swift_files:
    file_ref_uids[sf] = uid()
    build_file_uids[sf] = uid()

# ── Build group hierarchy ──
# We need to create groups matching the directory structure
groups = {}  # dir_path -> { uid, children: [child_uid...], name }

def ensure_group(dir_path):
    if dir_path in groups:
        return groups[dir_path]
    g = {"uid": uid(), "children": [], "name": os.path.basename(dir_path) if dir_path else PROJECT_NAME, "path": dir_path}
    groups[dir_path] = g
    # Ensure parent
    parent = os.path.dirname(dir_path)
    if dir_path and parent != dir_path:
        pg = ensure_group(parent)
        if g["uid"] not in pg["children"]:
            pg["children"].append(g["uid"])
    return g

# Root group for source files
root_src_group = ensure_group("")

# Add files to groups
for sf in swift_files:
    d = os.path.dirname(sf)
    g = ensure_group(d)
    g["children"].append(file_ref_uids[sf])

# Add Assets.xcassets to root group
root_src_group["children"].append(assets_ref_uid)
# Add Preview Content group
root_src_group["children"].append(preview_group_uid)

# ── Generate PBX sections ──

def pbx_file_ref(uid_val, name, path, file_type="sourcecode.swift", source_tree='"<group>"'):
    return f'\t\t{uid_val} /* {name} */ = {{isa = PBXFileReference; lastKnownFileType = {file_type}; path = "{path}"; sourceTree = {source_tree}; }};'

def pbx_build_file(bf_uid, fr_uid, name):
    return f'\t\t{bf_uid} /* {name} in Sources */ = {{isa = PBXBuildFile; fileRef = {fr_uid} /* {name} */; }};'

lines_build_file = []
lines_file_ref = []
lines_sources = []

for sf in swift_files:
    name = os.path.basename(sf)
    lines_build_file.append(pbx_build_file(build_file_uids[sf], file_ref_uids[sf], name))
    # path is just the filename since groups provide the directory context
    lines_file_ref.append(pbx_file_ref(file_ref_uids[sf], name, name))
    lines_sources.append(f'\t\t\t\t{build_file_uids[sf]} /* {name} in Sources */,')

# Assets file reference
lines_file_ref.append(f'\t\t{assets_ref_uid} /* {ASSETS_CATALOG} */ = {{isa = PBXFileReference; lastKnownFileType = folder.assetcatalog; path = {ASSETS_CATALOG}; sourceTree = "<group>"; }};')
# Preview Assets
lines_file_ref.append(f'\t\t{preview_assets_ref_uid} /* Preview Assets.xcassets */ = {{isa = PBXFileReference; lastKnownFileType = folder.assetcatalog; path = "Preview Assets.xcassets"; sourceTree = "<group>"; }};')
# Product
lines_file_ref.append(f'\t\t{product_ref_uid} /* {PROJECT_NAME}.app */ = {{isa = PBXFileReference; explicitFileType = wrapper.application; includeInIndex = 0; path = {PROJECT_NAME}.app; sourceTree = BUILT_PRODUCTS_DIR; }};')

# ── Groups ──
lines_group = []

# Build PBXGroup entries for directory groups
for dir_path in sorted(groups.keys(), key=lambda x: (x.count(os.sep), x)):
    g = groups[dir_path]
    children_str = "\n".join([f"\t\t\t\t{c} /* */," for c in g["children"]])
    if dir_path == "":
        # Root source group - use path = project name for Xcode
        lines_group.append(f"""\t\t{g["uid"]} /* {PROJECT_NAME} */ = {{
\t\t\tisa = PBXGroup;
\t\t\tchildren = (
{children_str}
\t\t\t);
\t\t\tpath = {PROJECT_NAME};
\t\t\tsourceTree = "<group>";
\t\t}};""")
    else:
        lines_group.append(f"""\t\t{g["uid"]} /* {g["name"]} */ = {{
\t\t\tisa = PBXGroup;
\t\t\tchildren = (
{children_str}
\t\t\t);
\t\t\tpath = {g["name"]};
\t\t\tsourceTree = "<group>";
\t\t}};""")

# Preview Content group
lines_group.append(f"""\t\t{preview_group_uid} /* Preview Content */ = {{
\t\t\tisa = PBXGroup;
\t\t\tchildren = (
\t\t\t\t{preview_assets_ref_uid} /* Preview Assets.xcassets */,
\t\t\t);
\t\t\tpath = "Preview Content";
\t\t\tsourceTree = "<group>";
\t\t}};""")

# Main group (contains LichSo/ + Products/)
lines_group.append(f"""\t\t{root_group_uid} = {{
\t\t\tisa = PBXGroup;
\t\t\tchildren = (
\t\t\t\t{groups[""]["uid"]} /* {PROJECT_NAME} */,
\t\t\t\t{products_group_uid} /* Products */,
\t\t\t);
\t\t\tsourceTree = "<group>";
\t\t}};""")

# Products group
lines_group.append(f"""\t\t{products_group_uid} /* Products */ = {{
\t\t\tisa = PBXGroup;
\t\t\tchildren = (
\t\t\t\t{product_ref_uid} /* {PROJECT_NAME}.app */,
\t\t\t);
\t\t\tname = Products;
\t\t\tsourceTree = "<group>";
\t\t}};""")

# Frameworks group (empty)
lines_group.append(f"""\t\t{frameworks_group_uid} /* Frameworks */ = {{
\t\t\tisa = PBXGroup;
\t\t\tchildren = (
\t\t\t);
\t\t\tname = Frameworks;
\t\t\tsourceTree = "<group>";
\t\t}};""")

# ── Generate the full project.pbxproj ──
sources_files_str = "\n".join(lines_sources)
build_files_str = "\n".join(lines_build_file)
file_refs_str = "\n".join(lines_file_ref)
groups_str = "\n".join(lines_group)

pbxproj = f"""// !$*UTF8*$!
{{
\tarchiveVersion = 1;
\tclasses = {{
\t}};
\tobjectVersion = 56;
\tobjects = {{

/* Begin PBXBuildFile section */
{build_files_str}
/* End PBXBuildFile section */

/* Begin PBXFileReference section */
{file_refs_str}
/* End PBXFileReference section */

/* Begin PBXFrameworksBuildPhase section */
\t\t{frameworks_phase_uid} /* Frameworks */ = {{
\t\t\tisa = PBXFrameworksBuildPhase;
\t\t\tbuildActionMask = 2147483647;
\t\t\tfiles = (
\t\t\t);
\t\t\trunOnlyForDeploymentPostprocessing = 0;
\t\t}};
/* End PBXFrameworksBuildPhase section */

/* Begin PBXGroup section */
{groups_str}
/* End PBXGroup section */

/* Begin PBXNativeTarget section */
\t\t{target_uid} /* {PROJECT_NAME} */ = {{
\t\t\tisa = PBXNativeTarget;
\t\t\tbuildConfigurationList = {target_config_list_uid} /* Build configuration list for PBXNativeTarget "{PROJECT_NAME}" */;
\t\t\tbuildPhases = (
\t\t\t\t{sources_phase_uid} /* Sources */,
\t\t\t\t{frameworks_phase_uid} /* Frameworks */,
\t\t\t\t{resources_phase_uid} /* Resources */,
\t\t\t);
\t\t\tbuildRules = (
\t\t\t);
\t\t\tdependencies = (
\t\t\t);
\t\t\tname = {PROJECT_NAME};
\t\t\tproductName = {PROJECT_NAME};
\t\t\tproductReference = {product_ref_uid} /* {PROJECT_NAME}.app */;
\t\t\tproductType = "com.apple.product-type.application";
\t\t}};
/* End PBXNativeTarget section */

/* Begin PBXProject section */
\t\t{proj_uid} /* Project object */ = {{
\t\t\tisa = PBXProject;
\t\t\tattributes = {{
\t\t\t\tBuildIndependentTargetsInParallel = 1;
\t\t\t\tLastSwiftUpdateCheck = 1540;
\t\t\t\tLastUpgradeCheck = 1540;
\t\t\t\tTargetAttributes = {{
\t\t\t\t\t{target_uid} = {{
\t\t\t\t\t\tCreatedOnToolsVersion = 15.4;
\t\t\t\t\t}};
\t\t\t\t}};
\t\t\t}};
\t\t\tbuildConfigurationList = {proj_config_list_uid} /* Build configuration list for PBXProject "{PROJECT_NAME}" */;
\t\t\tcompatibilityVersion = "Xcode 14.0";
\t\t\tdevelopmentRegion = vi;
\t\t\thasScannedForEncodings = 0;
\t\t\tknownRegions = (
\t\t\t\ten,
\t\t\t\tvi,
\t\t\t\tBase,
\t\t\t);
\t\t\tmainGroup = {root_group_uid};
\t\t\tproductRefGroup = {products_group_uid} /* Products */;
\t\t\tprojectDirPath = "";
\t\t\tprojectRoot = "";
\t\t\ttargets = (
\t\t\t\t{target_uid} /* {PROJECT_NAME} */,
\t\t\t);
\t\t}};
/* End PBXProject section */

/* Begin PBXResourcesBuildPhase section */
\t\t{resources_phase_uid} /* Resources */ = {{
\t\t\tisa = PBXResourcesBuildPhase;
\t\t\tbuildActionMask = 2147483647;
\t\t\tfiles = (
\t\t\t);
\t\t\trunOnlyForDeploymentPostprocessing = 0;
\t\t}};
/* End PBXResourcesBuildPhase section */

/* Begin PBXSourcesBuildPhase section */
\t\t{sources_phase_uid} /* Sources */ = {{
\t\t\tisa = PBXSourcesBuildPhase;
\t\t\tbuildActionMask = 2147483647;
\t\t\tfiles = (
{sources_files_str}
\t\t\t);
\t\t\trunOnlyForDeploymentPostprocessing = 0;
\t\t}};
/* End PBXSourcesBuildPhase section */

/* Begin XCBuildConfiguration section */
\t\t{proj_debug_config_uid} /* Debug */ = {{
\t\t\tisa = XCBuildConfiguration;
\t\t\tbuildSettings = {{
\t\t\t\tALWAYS_SEARCH_USER_PATHS = NO;
\t\t\t\tASSTTAGS_COMPILATION_CONDITIONS = DEBUG;
\t\t\t\tCLANG_ANALYZER_NONNULL = YES;
\t\t\t\tCLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;
\t\t\t\tCLANG_CXX_LANGUAGE_STANDARD = "gnu++20";
\t\t\t\tCLANG_ENABLE_MODULES = YES;
\t\t\t\tCLANG_ENABLE_OBJC_ARC = YES;
\t\t\t\tCLANG_ENABLE_OBJC_WEAK = YES;
\t\t\t\tCLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;
\t\t\t\tCLANG_WARN_BOOL_CONVERSION = YES;
\t\t\t\tCLANG_WARN_COMMA = YES;
\t\t\t\tCLANG_WARN_CONSTANT_CONVERSION = YES;
\t\t\t\tCLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;
\t\t\t\tCLANG_WARN_DIRECT_OBJC_ISA_USAGE = YES_ERROR;
\t\t\t\tCLANG_WARN_DOCUMENTATION_COMMENTS = YES;
\t\t\t\tCLANG_WARN_EMPTY_BODY = YES;
\t\t\t\tCLANG_WARN_ENUM_CONVERSION = YES;
\t\t\t\tCLANG_WARN_INFINITE_RECURSION = YES;
\t\t\t\tCLANG_WARN_INT_CONVERSION = YES;
\t\t\t\tCLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;
\t\t\t\tCLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;
\t\t\t\tCLANG_WARN_OBJC_LITERAL_CONVERSION = YES;
\t\t\t\tCLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;
\t\t\t\tCLANG_WARN_QUOTED_INCLUDE_IN_FRAMEWORK_HEADER = YES;
\t\t\t\tCLANG_WARN_RANGE_LOOP_ANALYSIS = YES;
\t\t\t\tCLANG_WARN_STRICT_PROTOTYPES = YES;
\t\t\t\tCLANG_WARN_SUSPICIOUS_MOVE = YES;
\t\t\t\tCLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;
\t\t\t\tCLANG_WARN_UNREACHABLE_CODE = YES;
\t\t\t\tCLANG_WARN__DUPLICATE_METHOD_MATCH = YES;
\t\t\t\tCOPY_PHASE_STRIP = NO;
\t\t\t\tDEBUG_INFORMATION_FORMAT = dwarf;
\t\t\t\tENABLE_STRICT_OBJC_MSGSEND = YES;
\t\t\t\tENABLE_TESTABILITY = YES;
\t\t\t\tENABLE_USER_SCRIPT_SANDBOXING = YES;
\t\t\t\tGCC_C_LANGUAGE_STANDARD = gnu17;
\t\t\t\tGCC_DYNAMIC_NO_PIC = NO;
\t\t\t\tGCC_NO_COMMON_BLOCKS = YES;
\t\t\t\tGCC_OPTIMIZATION_LEVEL = 0;
\t\t\t\tGCC_PREPROCESSOR_DEFINITIONS = (
\t\t\t\t\t"DEBUG=1",
\t\t\t\t\t"$(inherited)",
\t\t\t\t);
\t\t\t\tGCC_WARN_64_TO_32_BIT_CONVERSION = YES;
\t\t\t\tGCC_WARN_ABOUT_RETURN_TYPE = YES_ERROR;
\t\t\t\tGCC_WARN_UNDECLARED_SELECTOR = YES;
\t\t\t\tGCC_WARN_UNINITIALIZED_AUTOS = YES_AGGRESSIVE;
\t\t\t\tGCC_WARN_UNUSED_FUNCTION = YES;
\t\t\t\tGCC_WARN_UNUSED_VARIABLE = YES;
\t\t\t\tIPHONEOS_DEPLOYMENT_TARGET = {DEPLOY_TARGET};
\t\t\t\tMTL_ENABLE_DEBUG_INFO = INCLUDE_SOURCE;
\t\t\t\tMTL_FAST_MATH = YES;
\t\t\t\tONLY_ACTIVE_ARCH = YES;
\t\t\t\tSDKROOT = iphoneos;
\t\t\t\tSWIFT_ACTIVE_COMPILATION_CONDITIONS = "DEBUG $(inherited)";
\t\t\t\tSWIFT_OPTIMIZATION_LEVEL = "-Onone";
\t\t\t}};
\t\t\tname = Debug;
\t\t}};
\t\t{proj_release_config_uid} /* Release */ = {{
\t\t\tisa = XCBuildConfiguration;
\t\t\tbuildSettings = {{
\t\t\t\tALWAYS_SEARCH_USER_PATHS = NO;
\t\t\t\tASSTTAGS_COMPILATION_CONDITIONS = "";
\t\t\t\tCLANG_ANALYZER_NONNULL = YES;
\t\t\t\tCLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;
\t\t\t\tCLANG_CXX_LANGUAGE_STANDARD = "gnu++20";
\t\t\t\tCLANG_ENABLE_MODULES = YES;
\t\t\t\tCLANG_ENABLE_OBJC_ARC = YES;
\t\t\t\tCLANG_ENABLE_OBJC_WEAK = YES;
\t\t\t\tCLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;
\t\t\t\tCLANG_WARN_BOOL_CONVERSION = YES;
\t\t\t\tCLANG_WARN_COMMA = YES;
\t\t\t\tCLANG_WARN_CONSTANT_CONVERSION = YES;
\t\t\t\tCLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;
\t\t\t\tCLANG_WARN_DIRECT_OBJC_ISA_USAGE = YES_ERROR;
\t\t\t\tCLANG_WARN_DOCUMENTATION_COMMENTS = YES;
\t\t\t\tCLANG_WARN_EMPTY_BODY = YES;
\t\t\t\tCLANG_WARN_ENUM_CONVERSION = YES;
\t\t\t\tCLANG_WARN_INFINITE_RECURSION = YES;
\t\t\t\tCLANG_WARN_INT_CONVERSION = YES;
\t\t\t\tCLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;
\t\t\t\tCLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;
\t\t\t\tCLANG_WARN_OBJC_LITERAL_CONVERSION = YES;
\t\t\t\tCLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;
\t\t\t\tCLANG_WARN_QUOTED_INCLUDE_IN_FRAMEWORK_HEADER = YES;
\t\t\t\tCLANG_WARN_RANGE_LOOP_ANALYSIS = YES;
\t\t\t\tCLANG_WARN_STRICT_PROTOTYPES = YES;
\t\t\t\tCLANG_WARN_SUSPICIOUS_MOVE = YES;
\t\t\t\tCLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;
\t\t\t\tCLANG_WARN_UNREACHABLE_CODE = YES;
\t\t\t\tCLANG_WARN__DUPLICATE_METHOD_MATCH = YES;
\t\t\t\tCOPY_PHASE_STRIP = NO;
\t\t\t\tDEBUG_INFORMATION_FORMAT = "dwarf-with-dsym";
\t\t\t\tENABLE_NS_ASSERTIONS = NO;
\t\t\t\tENABLE_STRICT_OBJC_MSGSEND = YES;
\t\t\t\tENABLE_USER_SCRIPT_SANDBOXING = YES;
\t\t\t\tGCC_C_LANGUAGE_STANDARD = gnu17;
\t\t\t\tGCC_NO_COMMON_BLOCKS = YES;
\t\t\t\tGCC_WARN_64_TO_32_BIT_CONVERSION = YES;
\t\t\t\tGCC_WARN_ABOUT_RETURN_TYPE = YES_ERROR;
\t\t\t\tGCC_WARN_UNDECLARED_SELECTOR = YES;
\t\t\t\tGCC_WARN_UNINITIALIZED_AUTOS = YES_AGGRESSIVE;
\t\t\t\tGCC_WARN_UNUSED_FUNCTION = YES;
\t\t\t\tGCC_WARN_UNUSED_VARIABLE = YES;
\t\t\t\tIPHONEOS_DEPLOYMENT_TARGET = {DEPLOY_TARGET};
\t\t\t\tMTL_ENABLE_DEBUG_INFO = NO;
\t\t\t\tMTL_FAST_MATH = YES;
\t\t\t\tSDKROOT = iphoneos;
\t\t\t\tSWIFT_COMPILATION_MODE = wholemodule;
\t\t\t\tVALIDATE_PRODUCT = YES;
\t\t\t}};
\t\t\tname = Release;
\t\t}};
\t\t{target_debug_config_uid} /* Debug */ = {{
\t\t\tisa = XCBuildConfiguration;
\t\t\tbuildSettings = {{
\t\t\t\tASSETCATALOG_COMPILER_APPICON_NAME = AppIcon;
\t\t\t\tASSETCATALOG_COMPILER_GLOBAL_ACCENT_COLOR_NAME = AccentColor;
\t\t\t\tCODE_SIGN_STYLE = Automatic;
\t\t\t\tCURRENT_PROJECT_VERSION = 1;
\t\t\t\tDEVELOPMENT_ASSET_PATHS = "\\"LichSo/Preview Content\\"";
\t\t\t\tENABLE_PREVIEWS = YES;
\t\t\t\tGENERATE_INFOPLIST_FILE = YES;
\t\t\t\tINFOPLIST_KEY_CFBundleDisplayName = "Lịch Số";
\t\t\t\tINFOPLIST_KEY_LSApplicationCategoryType = "public.app-category.lifestyle";
\t\t\t\tINFOPLIST_KEY_UIApplicationSceneManifest_Generation = YES;
\t\t\t\tINFOPLIST_KEY_UIApplicationSupportsIndirectInputEvents = YES;
\t\t\t\tINFOPLIST_KEY_UILaunchScreen_Generation = YES;
\t\t\t\tINFOPLIST_KEY_UISupportedInterfaceOrientations = UIInterfaceOrientationPortrait;
\t\t\t\tINFOPLIST_KEY_UISupportedInterfaceOrientations_iPad = "UIInterfaceOrientationLandscapeLeft UIInterfaceOrientationLandscapeRight UIInterfaceOrientationPortrait UIInterfaceOrientationPortraitUpsideDown";
\t\t\t\tLD_RUNPATH_SEARCH_PATHS = (
\t\t\t\t\t"$(inherited)",
\t\t\t\t\t"@executable_path/Frameworks",
\t\t\t\t);
\t\t\t\tMARKETING_VERSION = 1.0;
\t\t\t\tPRODUCT_BUNDLE_IDENTIFIER = {BUNDLE_ID};
\t\t\t\tPRODUCT_NAME = "$(TARGET_NAME)";
\t\t\t\tSWIFT_EMIT_LOC_STRINGS = YES;
\t\t\t\tSWIFT_VERSION = {SWIFT_VERSION};
\t\t\t\tTARGETED_DEVICE_FAMILY = "1,2";
\t\t\t}};
\t\t\tname = Debug;
\t\t}};
\t\t{target_release_config_uid} /* Release */ = {{
\t\t\tisa = XCBuildConfiguration;
\t\t\tbuildSettings = {{
\t\t\t\tASSETCATALOG_COMPILER_APPICON_NAME = AppIcon;
\t\t\t\tASSETCATALOG_COMPILER_GLOBAL_ACCENT_COLOR_NAME = AccentColor;
\t\t\t\tCODE_SIGN_STYLE = Automatic;
\t\t\t\tCURRENT_PROJECT_VERSION = 1;
\t\t\t\tDEVELOPMENT_ASSET_PATHS = "\\"LichSo/Preview Content\\"";
\t\t\t\tENABLE_PREVIEWS = YES;
\t\t\t\tGENERATE_INFOPLIST_FILE = YES;
\t\t\t\tINFOPLIST_KEY_CFBundleDisplayName = "Lịch Số";
\t\t\t\tINFOPLIST_KEY_LSApplicationCategoryType = "public.app-category.lifestyle";
\t\t\t\tINFOPLIST_KEY_UIApplicationSceneManifest_Generation = YES;
\t\t\t\tINFOPLIST_KEY_UIApplicationSupportsIndirectInputEvents = YES;
\t\t\t\tINFOPLIST_KEY_UILaunchScreen_Generation = YES;
\t\t\t\tINFOPLIST_KEY_UISupportedInterfaceOrientations = UIInterfaceOrientationPortrait;
\t\t\t\tINFOPLIST_KEY_UISupportedInterfaceOrientations_iPad = "UIInterfaceOrientationLandscapeLeft UIInterfaceOrientationLandscapeRight UIInterfaceOrientationPortrait UIInterfaceOrientationPortraitUpsideDown";
\t\t\t\tLD_RUNPATH_SEARCH_PATHS = (
\t\t\t\t\t"$(inherited)",
\t\t\t\t\t"@executable_path/Frameworks",
\t\t\t\t);
\t\t\t\tMARKETING_VERSION = 1.0;
\t\t\t\tPRODUCT_BUNDLE_IDENTIFIER = {BUNDLE_ID};
\t\t\t\tPRODUCT_NAME = "$(TARGET_NAME)";
\t\t\t\tSWIFT_EMIT_LOC_STRINGS = YES;
\t\t\t\tSWIFT_VERSION = {SWIFT_VERSION};
\t\t\t\tTARGETED_DEVICE_FAMILY = "1,2";
\t\t\t}};
\t\t\tname = Release;
\t\t}};
/* End XCBuildConfiguration section */

/* Begin XCConfigurationList section */
\t\t{proj_config_list_uid} /* Build configuration list for PBXProject "{PROJECT_NAME}" */ = {{
\t\t\tisa = XCConfigurationList;
\t\t\tbuildConfigurations = (
\t\t\t\t{proj_debug_config_uid} /* Debug */,
\t\t\t\t{proj_release_config_uid} /* Release */,
\t\t\t);
\t\t\tdefaultConfigurationIsVisible = 0;
\t\t\tdefaultConfigurationName = Release;
\t\t}};
\t\t{target_config_list_uid} /* Build configuration list for PBXNativeTarget "{PROJECT_NAME}" */ = {{
\t\t\tisa = XCConfigurationList;
\t\t\tbuildConfigurations = (
\t\t\t\t{target_debug_config_uid} /* Debug */,
\t\t\t\t{target_release_config_uid} /* Release */,
\t\t\t);
\t\t\tdefaultConfigurationIsVisible = 0;
\t\t\tdefaultConfigurationName = Release;
\t\t}};
/* End XCConfigurationList section */
\t}};
\trootObject = {proj_uid} /* Project object */;
}}
"""

# ── Write output ──
proj_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), f"{PROJECT_NAME}.xcodeproj")
os.makedirs(proj_dir, exist_ok=True)

out_path = os.path.join(proj_dir, "project.pbxproj")
with open(out_path, "w") as f:
    f.write(pbxproj)

print(f"✅ Generated: {out_path}")
print(f"   {len(swift_files)} Swift files included")

# ── Create Assets.xcassets ──
assets_dir = os.path.join(SRC_ROOT, "Assets.xcassets")
os.makedirs(assets_dir, exist_ok=True)
with open(os.path.join(assets_dir, "Contents.json"), "w") as f:
    f.write('{\n  "info" : {\n    "author" : "xcode",\n    "version" : 1\n  }\n}\n')

# AccentColor
accent_dir = os.path.join(assets_dir, "AccentColor.colorset")
os.makedirs(accent_dir, exist_ok=True)
with open(os.path.join(accent_dir, "Contents.json"), "w") as f:
    f.write("""{
  "colors" : [
    {
      "color" : {
        "color-space" : "srgb",
        "components" : { "red" : "0.718", "green" : "0.110", "blue" : "0.110", "alpha" : "1.000" }
      },
      "idiom" : "universal"
    }
  ],
  "info" : { "author" : "xcode", "version" : 1 }
}
""")

# AppIcon
appicon_dir = os.path.join(assets_dir, "AppIcon.appiconset")
os.makedirs(appicon_dir, exist_ok=True)
with open(os.path.join(appicon_dir, "Contents.json"), "w") as f:
    f.write("""{
  "images" : [
    {
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    }
  ],
  "info" : { "author" : "xcode", "version" : 1 }
}
""")

# ── Create Preview Content ──
preview_dir = os.path.join(SRC_ROOT, "Preview Content", "Preview Assets.xcassets")
os.makedirs(preview_dir, exist_ok=True)
with open(os.path.join(preview_dir, "Contents.json"), "w") as f:
    f.write('{\n  "info" : {\n    "author" : "xcode",\n    "version" : 1\n  }\n}\n')

print("✅ Assets.xcassets and Preview Content created")
print(f"\n🎉 Open in Xcode: open {proj_dir}")
