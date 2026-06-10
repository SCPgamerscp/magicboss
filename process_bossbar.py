from PIL import Image

def process():
    # 1. Load the user's prepared image (256x236)
    # The TOP half (0-118) is the empty frame.
    # The BOTTOM half (118-236) has the purple fill.
    img = Image.open('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png').convert('RGBA')
    w, h = img.size
    h_half = h // 2
    
    # We strip any hard black background pixels that might have snuck in
    pixels = img.load()
    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if r < 10 and g < 10 and b < 10:
                pixels[x, y] = (0, 0, 0, 0)
    
    # 2. Extract the fill area in the bottom half (Full state)
    bottom = img.crop((0, h_half, w, h))
    px_b = bottom.load()
    fill = None
    for y in range(h_half):
        for x in range(w):
            r, g, b, a = px_b[x, y]
            if a > 50 and b > 150 and r > 100: # Purple Detection
                if not fill: fill = [x, y, x, y]
                else:
                    fill[0] = min(fill[0], x); fill[1] = min(fill[1], y)
                    fill[2] = max(fill[2], x); fill[3] = max(fill[3], y)
    
    img.save('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png')
    
    print("--- DATA FOR JAVA ---")
    print(f"W: {w}, H_S: {h_half}")
    if fill:
        print(f"FILL_X: {fill[0]}, FILL_Y: {fill[1]}, FILL_W: {fill[2]-fill[0]+1}, FILL_H: {fill[3]-fill[1]+1}")

process()
