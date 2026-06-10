from PIL import Image

def check_top_half():
    img = Image.open('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png').convert('RGBA')
    w, h = img.size
    h_s = h // 2
    
    # Top Half (Background)
    top = img.crop((0, 0, w, h_s))
    
    # Check for purple in the "fill" area we detected (8, 4, 240, 83)
    # Let's sample the center
    cx, cy = w // 2, h_s // 2
    pixel = top.getpixel((cx, cy))
    
    print(f"Top Half Center Pixel ({cx}, {cy}): {pixel}")
    
    # Count purple pixels
    purple_count = 0
    total_count = 0
    pixels = top.load()
    for y in range(h_s):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if a > 50 and b > 100 and b > g: # Purple
                purple_count += 1
            if a > 10:
                total_count += 1
                
    print(f"Total Visible Pixels: {total_count}")
    print(f"Purple Pixels in Top Half: {purple_count}")
    
    if purple_count > total_count * 0.1:
        print("CONCLUSION: Top half contains significant purple content! It might be pre-filled.")
    else:
        print("CONCLUSION: Top half seems mostly empty of purple.")

if __name__ == '__main__':
    check_top_half()
