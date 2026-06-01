import os
import glob

files = glob.glob('android/app/src/main/java/com/lichso/app/**/*.kt', recursive=True)
for file in files:
    with open(file, 'r') as f:
        content = f.read()
    
    if 'background(c.bg)' in content or 'background(Bg)' in content:
        new_content = content.replace('background(c.bg)', 'screenBackground(c.bg)')
        new_content = new_content.replace('background(Bg)', 'screenBackground(Bg)')
        
        # Add import if needed
        if 'import com.lichso.app.ui.theme.screenBackground' not in new_content:
            # find first import
            idx = new_content.find('import ')
            if idx != -1:
                new_content = new_content[:idx] + 'import com.lichso.app.ui.theme.screenBackground\n' + new_content[idx:]
                
        with open(file, 'w') as f:
            f.write(new_content)
