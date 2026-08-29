import re

for filename in ['app/src/main/res/values/strings.xml', 'app/src/main/res/values-en/strings.xml']:
    with open(filename, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Remove duplicate <string name="back">...
    seen_keys = set()
    new_lines = []
    for line in content.split('\n'):
        match = re.search(r'<string name="(.*?)">', line)
        if match:
            key = match.group(1)
            if key in seen_keys:
                continue
            seen_keys.add(key)
        new_lines.append(line)
        
    with open(filename, 'w', encoding='utf-8') as f:
        f.write('\n'.join(new_lines))
