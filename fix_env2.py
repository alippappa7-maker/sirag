with open('.env.example', 'r') as f:
    lines = f.readlines()

with open('.env.example', 'w') as f:
    for line in lines:
        if line.startswith('GOOGLE_PLAY_'):
            parts = line.split('=')
            f.write(parts[0] + '="placeholder"\n')
        else:
            f.write(line)
