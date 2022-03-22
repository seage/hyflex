#! /usr/bin/python3
# @author David Omrai

from cmath import exp
from matplotlib.colors import LinearSegmentedColormap
from importlib.abc import FileLoader
from xml.dom import minidom
from jinja2 import Environment, FileSystemLoader 
import sys
from numpy import double

supported_problems = ["SAT", "TSP", "FSP", "QAP"]

c = ["darkred", "red", "yellow", "green", "darkgreen"]
v = [0, .5, .65, .85, 1]
l = list(zip(v, c))
cmap = LinearSegmentedColormap.from_list('rg',l, N=256)

hh_info = {
    "ACO-HH": {
        "name": 'Ant colony optimization',
        "author": 'José Luis Núñez'
    },
    "AsapHH-GIHH": {
        "name": 'Genetic Iterative Hyper-heuristic',
        "author": 'Mustafa Misir'
    },
    "Ant-Q": {
        "name": '',
        "author": 'Imen Khamassi'
    },
    "AVEG-Nep": {
        "name": 'A Reinforcement Learning approach',
        "author": 'Luca Di Gaspero'
    },
    "Clean": {
        "name": '',
        "author": 'Mohamed Bader-El-Den'
    },
    "Clean-2": {
        "name": '',
        "author": 'Mohamed Bader-El-Den'
    },
    "DynILS": {
        "name": 'Dynamic Iterated Local Search',
        "author": 'Mark Johnston'
    },
    "DynILS-b": {
        "name": 'Non-Improvement Bias Iterated Local Search',
        "author": 'Mark Johnston'
    },
    "EPH": {
        "name": 'Evolutionary Programming Hyper-heuristic',
        "author": 'David Meignan'
    },
    "GenHive": {
        "name": 'Genetic Hive HyperHeuristic',
        "author": 'Michal Frankiewicz',
    },
    "GISS": {
        "name": 'Generic Iterative Simulated-Annealing Search',
        "author": 'Alberto Acuna'
    },
    "HAEA": {
        "name": 'Hybrid Adaptive Evolutionary Algorithm Hyper Heuristic',
        "author": 'Jonatan Gómez'
    },
    "HAHA": {
        "name": 'A new Hyperheuristic Algorithm',
        "author": 'Andreas Lehrbaum'
    },
    "ISEA": {
        "name": 'terated Local Search Driven by Evolutionary Algorithm',
        "author": 'Jiří Kubalík'
    },
    "KSATS-HH":{
        "name": 'KSATS-HH: A Simulated Annealing Hyper-Heuristic with Reinforcement Learning and Tabu-Search',
        "author": 'Kevin Sim'
    },
    "LeanGIHH": {
        "name": 'Simpler variant of GIHH',
        "author": 'Steven Adriaensen'
    },
    "MCHH-S": {
        "name": 'A Single Objective Variant of the Online Selective Markov chain Hyper-heuristic',
        "author": 'Kent McClymont'
    },
    "ML": {
        "name": 'Self-adaptive meta-heuristic of Meignan',
        "author": 'Mathieu Larose'
    },
    "NAHH":{
        "name": 'Non-Adaptive Hyper-Heuristic',
        "author": 'Franco Mascia'
    },
    "PHUNTER": {
        "name": 'Pearl Hunter: A Hyper-heuristic that Compiles Iterated Local Search Algorithms',
        "author": 'Fan Xue'
    },
    "SA_ILS": {
        "name": '',
        "author": 'He Jiang'
    },
    "SelfSearch": {
        "name": 'Self-Search (Extended Abstract)',
        "author": 'Jawad Elomari'
    },
    "VNS-TW": {
        "name": 'Variable neighborhood search-based hyperheuristic',
        "author": 'Ping-Che Hsiao'
    },
    "XCJ": {
        "name": 'eXplore-Climb-Jump: A Hill Climbing based Cross-Domain Hyper-Heuristi',
        "author": 'Kamran Shafi'
    },
}

def exp_xml_to_dict(exp_xml_path):
    # Try if file exists

    # Parse xml file
    results_xml = minidom.parse(exp_xml_path).getElementsByTagName('results')[0]

    algorithms_xml =  results_xml.getElementsByTagName("algorithm")
    problems_xml =algorithms_xml[0].getElementsByTagName("problem")

    # Extract the data from xml format
    problems = []

    for problem_name in supported_problems:
        name_found = any(x for x in problems_xml if x.getAttribute("name") == problem_name)
        if name_found:
            problems.append(problem_name)

    results = []

    for algorithm_xml in algorithms_xml:
        result = {}
        
        # Algorithm name
        result["name"] = algorithm_xml.getAttribute("name")

        # Algorithm overall score
        overall_score = double(format(double(algorithm_xml.getAttribute("score")), ".5f"))
        result["overall"] = overall_score
        result["overall_color"] = cmap(int(overall_score * 256))

        # Score on each problem
        result["score"] = {}
        result["color"] = {}
        for score_xml in algorithm_xml.getElementsByTagName("problem"):
            h_name = score_xml.getAttribute("name")
            h_score = double(format(double(score_xml.getAttribute("avg")), ".5f"))

            result["score"][h_name] = h_score

            result["color"][h_name] = cmap(int(h_score * 256))

            # todo colors
        results.append(result)

    results.sort(key=lambda k: k["overall"], reverse=True)

    return results, problems


def create_page(results, problems, page_dest):
    # Jinja2 part for templates
    file_loader = FileSystemLoader("scripts/heatmap")
    env = Environment(loader=file_loader)
    rendered = env.get_template("heatmap.template.svg").render(results=results, problems=problems, hh_info=hh_info)

    with open("{}".format(str(page_dest)), "w") as f:
        f.write(rendered)

def build_results_page(exp_id):
    results, problems = exp_xml_to_dict("results/{}/unit-metric-scores.xml".format(exp_id))
    create_page(results, problems, "results/{}/heatmap.svg".format(exp_id))


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Provide the id if the competition")
        sys.exit(1)
    build_results_page(sys.argv[1])