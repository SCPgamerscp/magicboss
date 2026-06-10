from PIL import Image, ImageChops

def detect_fill():
    img = Image.open('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png').convert('RGBA')
    w, h = img.size
    h_s = h // 2
    
    top = img.crop((0, 0, w, h_s))
    bot = img.crop((0, h_s, w, h))
    
    # Find the difference between top and bottom halves
    # This should highlight just the purple bar that was added in the 'full' state
    diff = ImageChops.difference(top, bot)
    bbox = diff.getbbox()
    
    print(f"Texture Size: {w}x{h}, StateH: {h_s}")
    print(f"Fill BBOX (relative to state top): {bbox}")
    
    if bbox:
        # Check the colors in that bbox to be sure it's purple
        fill_area = bot.crop(bbox)
        avg_color = fill_area.resize((1,1)).getpixel((0,0))
        print(f"Avg Fill Color: {avg_color}")
        
        # Output constants
        print("\n--- JAVA CONSTANTS ---")
        print(f"FILL_X = {bbox[0]}")
        print(f"FILL_Y = {bbox[1]}")
        print(f"FILL_WIDTH = {bbox[2] - bbox[0]}")
        print(f"FILL_HEIGHT = {bbox[3] - bbox[1]}")

if __name__ == '__main__':
    detect_fill()
