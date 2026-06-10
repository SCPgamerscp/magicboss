import json
import os

source_file = r"c:\Users\ecrea\Downloads\irons-spells-n-spellbooks-1.20.1-3.15.0\irons-spells-n-spellbooks-1.20.1-3.15.0\src\main\resources\assets\irons_spellbooks\animations\casting_animations.json"
target_file = r"c:\Users\ecrea\Downloads\magicboss\src\main\resources\assets\elementmagicboss\animations\angel_raziel.animation.json"
output_file = target_file

essential_anims = [
    "instant_projectile",
    "continuous_thrust",
    "charged_throw",
    "long_cast",
    "long_cast_finish",
    "instant_self",
    "horizontal_slash_one_handed",
    "stomp",
    "cross_arms",
    "cast_t_pose",
    "kneeling_prayer",
    "self_cast_two_hands",
    "continuous_thrust_one_handed",
    "throw_item",
    "overhead_two_handed_swing"
]

with open(source_file, 'r') as f:
    source_data = json.load(f)

with open(target_file, 'r') as f:
    target_data = json.load(f)

source_animations = source_data.get("animations", {})
target_animations = target_data.get("animations", {})

for anim_name in essential_anims:
    if anim_name in source_animations:
        print(f"Merging animation: {anim_name}")
        target_animations[anim_name] = source_animations[anim_name]
    else:
        print(f"Warning: Animation '{anim_name}' not found in source.")

target_data["animations"] = target_animations

with open(output_file, 'w') as f:
    json.dump(target_data, f, indent=4)

print("Done merging animations.")
