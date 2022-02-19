#! /usr/bin/python
# @author David Omrai

from cmath import exp
from importlib.abc import FileLoader
from xml.dom import minidom
from jinja2 import Environment, FileSystemLoader 
import sys
import os

hh_info = {
    "clean": {
        "name": '',
        "author": ''
    },
    "isea": {
        "name": '',
        "author": ''
    },
    "aco_hh": {
        "name": '',
        "author": ''
    },
    "elomariss": {
        "name": '',
        "author": ''
    },
    "simsats_hh":{
        "name": '',
        "author": ''
    },
    "clean02": {
        "name": '',
        "author": ''
    },
    "sa_ilshyperh{uristic": {
        "name": '',
        "author": ''
    },
    "eph": {
        "name": '',
        "author": ''
    },
    "pearlhunter": {
        "name": '',
        "author": ''
    },
    "haeahh": {
        "name": '',
        "author": ''
    },
    "urli_aveg_ne{tunehyperheuristic": {
        "name": '',
        "author": ''
    },
    "lehrbaumhaha": {
        "name": '',
        "author": ''
    },
    "johnstonbias{ls": {
        "name": '',
        "author": ''
    },
    "leangihh": {
        "name": '',
        "author": ''
    },
    "laroseml": {
        "name": '',
        "author": ''
    },
    "hsiaochescyp{rheuristic": {
        "name": '',
        "author": ''
    },
    "ant_q": {
        "name": '',
        "author": ''
    },
    "johnstondyna{icils": {
        "name": '',
        "author": ''
    },
    "shafixcj": {
        "name": '',
        "author": ''
    },
    "giss": {
        "name": '',
        "author": ''
    },
}

def exp_xml_to_dict(exp_xml_path):
    # Parse xml file
    results_xml = minidom.parse(exp_xml_path).getElementsByTagName('results')[0]

    algorithms_xml =  results_xml.getElementsByTagName("algorithm") 
    problems_xml =algorithms_xml[0].getElementsByTagName("problem")

    # Extract the data from xml format
    problems = []

    for problem_xml in problems_xml:
        problems.append(problem_xml.getAttribute("name"))

    results = []

    for algorithm_xml in algorithms_xml:
        result = {}
        
        # Algorithm name
        result["name"] = algorithm_xml.getAttribute("name")

        # Algorithm overall score
        result["overall"] = algorithm_xml.getAttribute("score")

        # Score on each problem
        result["score"] = {}
        for score_xml in algorithm_xml.getElementsByTagName("problem"):
            result["score"][score_xml.getAttribute("name")] = score_xml.getAttribute("avg")
        
        results.append(result)

    return results, problems


def create_page(results, problems, page_dest):
    # Jinja2 part for templates
    file_loader = FileSystemLoader("docs/pages/templates")
    env = Environment(loader=file_loader)
    rendered = env.get_template("results.html").render(results=results, problems=problems)

    with open("{}".format(str(page_dest)), "w") as f:
        f.write(rendered)

def build_results_page(exp_id):
    results, problems = exp_xml_to_dict("results/{}/unit-metric-scores.xml".format(str(exp_id)))
    create_page(results, problems, "results/{}/index.html".format(str(exp_id)))


if __name__ == "__main__":
    build_results_page(int(sys.argv[1]))