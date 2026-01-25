import json
import os
import requests
from tqdm import tqdm
from concurrent.futures import ThreadPoolExecutor
import random

# --- CONFIGURATION ---
MAX_WORKERS = 20
INPUT_FILE = 'app/src/main/assets/master_locations.json'
OUTPUT_FILE = 'app/src/main/assets/master_locations.json'

# --- HELPER FUNCTIONS ---

def get_wiki_geo_photo(lat, lng, radius=500):
    """Finds the closest actual photo taken near these coordinates."""
    try:
        url = "https://en.wikipedia.org/w/api.php"
        # Step 1: Search for the nearest page with coordinates
        geo_params = {
            "action": "query", "list": "geosearch", "gscoord": f"{lat}|{lng}",
            "gsradius": radius, "gslimit": 1, "format": "json"
        }
        geo_res = requests.get(url, params=geo_params, timeout=5).json()
        results = geo_res.get("query", {}).get("geosearch", [])

        if not results: return None

        # Step 2: Get the primary image for that nearby landmark
        page_title = results[0]['title']
        img_params = {
            "action": "query", "titles": page_title, "prop": "pageimages",
            "format": "json", "pithumbsize": 1000
        }
        img_res = requests.get(url, params=img_params, timeout=5).json()
        pages = img_res.get("query", {}).get("pages", {})
        for p in pages:
            if "thumbnail" in pages[p]:
                return pages[p]["thumbnail"]["source"]
    except Exception as e:
        # print(f"Wiki error: {e}")
        return None
    return None

def get_keywords(loc):
    """Extracts relevant keywords for image search."""
    category = loc.get('category', '')
    name = loc.get('name', '').lower()
    region = loc.get('region', '')

    keywords = []

    # Specific name-based keywords
    if 'wine' in name or 'winery' in name or 'cellar' in name or 'marani' in name:
        keywords.append('vineyard')
    elif 'church' in name or 'monastery' in name or 'cathedral' in name:
        keywords.append('church')
    elif 'lake' in name:
        keywords.append('lake')
    elif 'mountain' in name or 'peak' in name:
        keywords.append('mountain')
    elif 'restaurant' in name or 'cafe' in name or 'bar' in name:
        keywords.append('restaurant')
    elif 'hotel' in name or 'guesthouse' in name:
        keywords.append('hotel')

    # Category-based keywords
    if not keywords:
        cat_map = {
            "Dining & Nightlife": "restaurant,food",
            "Shopping": "market,shop",
            "Attractions & Activities": "landmark,tourism",
            "Transportation & Essential Services": "city,street"
        }
        if category in cat_map:
            keywords.append(cat_map[category])

    # Region fallback
    if region:
        keywords.append(region)

    keywords.append('georgia')

    return ",".join(keywords)

def get_loremflickr_url(loc_id, keywords):
    """Generates a consistent LoremFlickr URL."""
    # Use lock to ensure consistency for the same ID
    return f"https://loremflickr.com/800/600/{keywords}?lock={loc_id}"

def process_location(loc):
    # Update ALL images as requested
    # if loc.get('image') != "USE_LIVE_VIEW":
    #     return loc

    lat, lng = loc.get('lat'), loc.get('lng')
    loc_id = loc.get('id', 0)

    # Priority 1: Wikimedia Geo-Discovery (Real nearby photo)
    img = None
    if lat and lng:
        img = get_wiki_geo_photo(lat, lng)

    # Priority 2: Keyword-based Consistent Placeholder
    if not img:
        keywords = get_keywords(loc)
        img = get_loremflickr_url(loc_id, keywords)

    loc['image'] = img
    return loc

def run_pipeline():
    if not os.path.exists(INPUT_FILE):
        print(f"Input file not found: {INPUT_FILE}")
        return

    with open(INPUT_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    print(f"Processing {len(data)} locations...")

    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        # We need to map and collect results.
        # Since data is a list of dicts, we can process in place or create new list.
        # process_location returns the modified loc dict.
        results = list(tqdm(executor.map(process_location, data), total=len(data), desc="Updating Images"))

    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=4, ensure_ascii=False)

    print(f"Saved updated locations to {OUTPUT_FILE}")

if __name__ == "__main__":
    run_pipeline()
