from PIL import Image

def analyze_raw():
    img = Image.open('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/original_image.png').convert('RGBA')
    w, h = img.size
    
    # Bottom state (Full bar)
    h_half = h // 2
    bottom = img.crop((0, h_half, w, h))
    px = bottom.load()
    
    # 1. Find the horizontal line with most purple/pink pixels
    # We look for pixels where B > 150 and R > 150 (Vibrant purple)
    max_purple = 0
    best_y = 0
    for y in range(h_half):
        count = 0
        for x in range(w):
            r, g, b, a = px[x, y]
            if a > 100 and b > 150 and r > 100:
                count += 1
        if count > max_purple:
            max_purple = count
            best_y = y
            
    # 2. Find X bounds on that best_y line
    x_min = w
    x_max = 0
    for x in range(w):
        r, g, b, a = px[x, best_y]
        if a > 100 and b > 150 and r > 100:
            x_min = min(x_min, x)
            x_max = max(x_max, x)
            
    # 3. Find Y bounds around that line
    y_min = best_y
    y_max = best_y
    for y in range(h_half):
        found = False
        for x in range(x_min, x_max + 1):
            r, g, b, a = px[x, y]
            if a > 100 and b > 150 and r > 100:
                found = True
                break
        if found:
            y_min = min(y_min, y)
            y_max = max(y_max, y)

    # 4. Find the overall non-transparent content bounds for the state
    top = img.crop((0, 0, w, h_half))
    bbox = top.getbbox() # Frame bounds
    
    print(f"RAW_IMAGE: {w}x{h}")
    print(f"STATE_HEIGHT: {h_half}")
    print(f"BBOX: {bbox}")
    print(f"FILL_BOUNDS: x={x_min}, y={y_min}, w={x_max-x_min+1}, h={y_max-y_min+1}")

analyze_raw()
