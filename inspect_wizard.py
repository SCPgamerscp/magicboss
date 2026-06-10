from PIL import Image

def inspect_wizard():
    img = Image.open('src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png').convert('RGBA')
    w, h = img.size
    h_s = h // 2
    top = img.crop((0, 0, w, h_s))
    bot = img.crop((0, h_s, w, h))
    
    print(f"Size: {w}x{h}, StateH: {h_s}")
    print(f"Top BBOX: {top.getbbox()}")
    
    px = bot.load()
    fill = None
    for y in range(h_s):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a > 50 and b > 150: # Simple purple detection
                if not fill: fill = [x, y, x, y]
                else:
                    fill[0] = min(fill[0], x)
                    fill[1] = min(fill[1], y)
                    fill[2] = max(fill[2], x)
                    fill[3] = max(fill[3], y)
    print(f"Fill: {fill}")
    if fill:
        print(f"Fill Bounds: x={fill[0]}, y={fill[1]}, w={fill[2]-fill[0]+1}, h={fill[3]-fill[1]+1}")

if __name__ == '__main__':
    inspect_wizard()
