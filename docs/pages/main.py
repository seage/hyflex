#! /usr/bin/python
# @author David Omrai

from cmath import exp
from importlib.abc import FileLoader
from xml.dom import minidom
from jinja2 import Environment, FileSystemLoader 
import sys

def exp_xml_to_dict(exp_xml_path):
    # Parse xml file
    results_xml = minidom.parse(exp_xml_path).getElementsByTagName('results')[0]

    algorithms_xml =  results_xml.getElementsByTagName("algorithm") 
    problems_xml =algorithms_xml[0].getElementsByTagName("problem")

    # Extract the data from xml format
    problems = []

    for problem_xml in problems_xml:
        problems.append(problem_xml.attribute["name"])

    results = []

    for algorithm_xml in algorithms_xml:
        result = {}
        
        # Algorithm name
        result["name"] = algorithm_xml.attribute["name"]

        # Algorithm overall score
        result["overall"] = algorithm_xml.attribute["score"]

        # Score on each problem
        result["score"] = {}
        for score_xml in algorithm_xml.getElementsByTagName("problem"):
            result["score"][score_xml.attribute["name"]] = score_xml.attribute["avg"]
        
        results.append(result)


def create_page(results, page_dest):
    # Jinja2 part for templates
    file_loader = FileSystemLoader("templates")
    env = Environment(loader=file_loader)

    rendered = env.get_template("docs/pages/templates/results.html").render()

    with open("{page_dest}", "w") as f:
        f.write(rendered)

def build_results_page(exp_id):
    results = exp_xml_to_dict("results/{exp_id}/unit-metric-scores.xml")
    create_page(results, "results/{exp_id}/index.html")


if __name__ == "__main__":
    build_results_page(int(sys.argv[1]))