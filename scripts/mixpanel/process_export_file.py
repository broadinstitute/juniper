# takes as input a file of exported mixpanel events and formats it for import -- adding json array and splitting
# into  subfiles with < 2000 events in each
# usage: python3 process_export_file.py <input_file>
import sys
import subprocess
import glob
import os

input_file = sys.argv[1]
input_file_name = os.path.basename(input_file)
input_base_name = input_file_name.split(".")[0] + "_split_"

subprocess.run(["split", "-l", "1500", "-a", "2", input_file, input_base_name])
new_files = glob.glob(input_base_name + "*")
for new_file in new_files:
    # add a json suffix
    file_with_suffix = new_file + ".json"
    subprocess.run(["mv", new_file, file_with_suffix])
    # reformat the content as a json array (from a raw list of event objects)
    with open(file_with_suffix, "r") as f:
        lines = f.readlines()
    with open(file_with_suffix, "w") as f:
        f.write("[\n")
        f.write(",".join(lines))
        f.write("]\n")

