import argparse
import json


parser = argparse.ArgumentParser()
parser.add_argument("ndc_path", help="Path to file downloaded from https://open.fda.gov/apis/drug/ndc/download/")
parser.add_argument("output_path", help="Path to output JSON file")
args = parser.parse_args()

all_drugs = []
with open(args.ndc_path) as f:
    all_drugs = json.load(f).get("results")

def generate_generic_name(drug):
    try:
        return ", ".join(ingredient["name"].lower() for ingredient in drug["active_ingredients"])
    except KeyError:
        return None

unique_drugs = set(generate_generic_name(drug) for drug in all_drugs if drug["marketing_category"] in ("ANDA", "NDA", "BLA")) - set([None])

with open(args.output_path, "w") as f:
    json.dump(sorted(unique_drugs), f, indent=2)
