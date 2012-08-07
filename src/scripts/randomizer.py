import random

skip_until="Philio/GoMySQL"
f = open("list.txt").readlines()
projs = []
for line in f:
    if line.startswith("allProjects=["):
        projs = [x.strip() for x in line.strip()[13:len(line.strip())-1].split(",")]

ctr = 0
while ctr < len(projs):
    if projs[ctr] == skip_until:
        break
    ctr = ctr + 1

useProjs = projs[ctr:]
random.shuffle(useProjs)
print ",".join(useProjs)
