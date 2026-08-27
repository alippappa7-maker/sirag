with open('app/src/main/java/com/siraj/app/features/settings/presentation/ProfileScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
imports = set()

for line in lines:
    if line.startswith('import '):
        if line not in imports:
            imports.add(line)
            new_lines.append(line)
    else:
        new_lines.append(line)

with open('app/src/main/java/com/siraj/app/features/settings/presentation/ProfileScreen.kt', 'w') as f:
    f.writelines(new_lines)

