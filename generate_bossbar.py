from PIL import Image, ImageDraw

def draw_pixel_art(draw, x_start, y_start, pixels, scale=1):
    for y, row in enumerate(pixels):
        for x, color in enumerate(row):
            if color:
                if scale == 1:
                    draw.point((x_start + x, y_start + y), fill=color)
                else:
                    draw.rectangle([x_start + x*scale, y_start + y*scale, x_start + (x+1)*scale - 1, y_start + (y+1)*scale - 1], fill=color)

# 192x42 (21px height per state)
width = 192
height = 21
total_h = height * 2
img = Image.new('RGBA', (width, total_h), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Pro colors
GOLD = (212, 175, 55, 255)
DARK_GOLD = (150, 110, 40, 255)
P_BG = (25, 10, 45, 255)
P_FILL = (150, 80, 240, 255)
P_GLOW = (200, 160, 255, 255)
P_SHADOW = (60, 20, 100, 255)

def draw_bar(y_off, fill):
    # Main gothic frame
    draw.rectangle([12, y_off+5, width-13, y_off+height-6], fill=P_BG if not fill else None)
    
    # Ornamental border
    draw.rectangle([11, y_off+4, width-12, y_off+height-5], outline=DARK_GOLD, width=1)
    draw.rectangle([12, y_off+5, width-13, y_off+height-6], outline=GOLD, width=1)

    # Fill logic
    if fill:
        f_w = width - 26
        draw.rectangle([13, y_off+6, 13+f_w, y_off+height-7], fill=P_FILL)
        draw.line([13, y_off+6, 13+f_w, y_off+6], fill=P_GLOW, width=1)
        draw.line([13, y_off+height-7, 13+f_w, y_off+height-7], fill=P_SHADOW, width=1)

    # Wizard Hat Icon (Left)
    hat = [
        [None, None, GOLD, GOLD, GOLD, None, None],
        [None, GOLD, P_GLOW, P_GLOW, P_GLOW, GOLD, None],
        [None, GOLD, P_FILL, P_FILL, P_FILL, GOLD, None],
        [GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD],
        [GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD]
    ]
    draw_pixel_art(draw, 3, y_off+6, hat, scale=1)

    # Book Icon (Right)
    book = [
        [GOLD, GOLD, GOLD, GOLD, GOLD],
        [GOLD, (80, 40, 20, 255), (80, 40, 20, 255), (80, 40, 20, 255), GOLD],
        [GOLD, (80, 40, 20, 255), GOLD, (80, 40, 20, 255), GOLD],
        [GOLD, GOLD, GOLD, GOLD, GOLD]
    ]
    draw_pixel_art(draw, width-10, y_off+7, book, scale=1)

# Empty state
draw_bar(0, False)
# Full state (for the shader/blit sampling)
draw_bar(21, True)

output = 'src/main/resources/assets/elementmagicboss/textures/gui/boss_bars/wizard.png'
img.save(output)
print(f"Refined thematic texture saved to {output}")
