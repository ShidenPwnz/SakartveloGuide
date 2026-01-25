import json
import os
from tqdm import tqdm

INPUT_FILE = 'app/src/main/assets/master_locations.json'
OUTPUT_FILE = 'app/src/main/assets/master_locations.json'

def calculate_scores(loc):
    name = loc.get('name', '')
    desc_en = loc.get('desc_en', '')
    category = loc.get('category', '')
    region = loc.get('region', '')

    popularity = 0
    priority = 0

    # --- 1. Popularity Calculation ---

    # Base score by Category
    if category == "Attractions & Activities":
        popularity += 40
    elif category == "Dining & Nightlife" or category == "RESTAURANT":
        popularity += 30
    elif category == "Shopping":
        popularity += 20
    elif category == "Transportation & Essential Services":
        popularity += 10
    else:
        popularity += 5

    # Keyword Boost (Name and Description)
    text_to_search = (name + " " + desc_en).lower()

    high_value_keywords = [
        "museum", "castle", "fortress", "monastery", "cathedral", "church",
        "national park", "lake", "canyon", "waterfall", "cave", "palace",
        "bridge", "tower", "wine", "winery", "vineyard", "unesco", "heritage",
        "botanical garden", "ski resort", "resort"
    ]

    specific_hotspots = [
        "fabrika", "bassiani", "khinkali house", "rooms hotel", "stamba"
    ]

    essential_keywords = [
        "pharmacy", "hospital", "clinic", "police", "station", "airport", "metro", "railway"
    ]

    for kw in high_value_keywords:
        if kw in text_to_search:
            popularity += 20
            break # Only apply once per group

    for kw in specific_hotspots:
        if kw in text_to_search:
            popularity += 40
            break

    # Description Boost
    if len(desc_en) > 60 and "Verified location" not in desc_en:
        popularity += 10

    # Region Boost
    major_regions = ["Tbilisi", "Batumi", "Kutaisi", "Kazbegi", "Mestia", "Sighnaghi", "Telavi", "Borjomi", "Gudauri"]
    if any(r.lower() in region.lower() for r in major_regions):
        popularity += 10

    # Cap Popularity
    popularity = min(popularity, 100)

    # Specific Overrides (Manual Corrections)
    if "Fabrika" in name and "Tbilisi" in region:
        popularity = max(popularity, 90) # Ensure high popularity

    # --- 2. Priority Calculation ---

    is_essential = category == "Transportation & Essential Services" or any(kw in text_to_search for kw in essential_keywords)

    if is_essential:
        priority = 8 # High priority for essentials

    # Tourist priority mapping
    tourist_priority = 0
    if popularity >= 80:
        tourist_priority = 10
    elif popularity >= 60:
        tourist_priority = 8
    elif popularity >= 40:
        tourist_priority = 6
    elif popularity >= 20:
        tourist_priority = 4
    else:
        tourist_priority = 1

    # Take the higher of essential vs tourist priority, but cap at 10
    priority = max(priority, tourist_priority)
    priority = min(priority, 10)

    # Specific Overrides for Priority
    if "Fabrika" in name and "Tbilisi" in region:
        priority = 10

    loc['popularity'] = popularity
    loc['priority'] = priority

    return loc

def main():
    if not os.path.exists(INPUT_FILE):
        print(f"Error: {INPUT_FILE} not found.")
        return

    print(f"Reading from {INPUT_FILE}...")
    with open(INPUT_FILE, 'r', encoding='utf-8') as f:
        locations = json.load(f)

    print(f"Processing {len(locations)} locations...")
    updated_locations = []

    for loc in tqdm(locations):
        updated_locations.append(calculate_scores(loc))

    print(f"Writing to {OUTPUT_FILE}...")
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(updated_locations, f, indent=4, ensure_ascii=False)

    print("Done.")

if __name__ == "__main__":
    main()
