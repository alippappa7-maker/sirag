with open('app/src/main/java/com/siraj/app/features/subscription/presentation/SubscriptionScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.strip() == '"+':
        continue
    new_lines.append(line)

with open('app/src/main/java/com/siraj/app/features/subscription/presentation/SubscriptionScreen.kt', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
