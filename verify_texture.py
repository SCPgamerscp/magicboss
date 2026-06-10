from PIL import Image

def verify_tex():
    try:
        img = Image.open('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png').convert('RGBA')
    except Exception as e:
        print(f"Error opening texture: {e}")
        return

    w, h = img.size
    print(f"Texture Size: {w}x{h}")
    
    # Constants from previous run
    STATE_HEIGHT = 85
    FILL_X = 16
    FILL_Y = 42
    FILL_W = 224
    FILL_H = 15
    
    # Check bottom half (Full state)
    # Expected absolute Y = STATE_HEIGHT + FILL_Y
    abs_y = STATE_HEIGHT + FILL_Y
    
    # Sample center of the fill bar
    sample_x = FILL_X + FILL_W // 2
    sample_y = abs_y + FILL_H // 2
    
    print(f"Sampling at ({sample_x}, {sample_y}) - Expected Fill Area")
    
    if sample_x >= w or sample_y >= h:
        print("Sample coordinates out of bounds!")
        return
        
    pixel = img.getpixel((sample_x, sample_y))
    print(f"Pixel Value: {pixel}")
    
    r, g, b, a = pixel
    if a > 50 and b > 100:
        print("VERIFIED: Pixel is Purple/Blue.")
    else:
        print("FAILURE: Pixel is NOT purple/visible. The texture generation might have failed.")

if __name__ == '__main__':
    verify_tex()
