with open('.env.example', 'r') as f:
    lines = f.readlines()

with open('.env.example', 'w') as f:
    for line in lines:
        if line.startswith('GOOGLE_PLAY_'):
            f.write(line.strip() + '="placeholder"\n')
        else:
            f.write(line)
