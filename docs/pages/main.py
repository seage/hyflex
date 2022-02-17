from importlib.abc import FileLoader
from xml.dom import minidom
from jinja2 import Environment, FileSystemLoader 

# parse xml file
# results = minidom.parse("results/1/unit-metric-scores.xml").getElementsByTagName('results')[0]

# print(results.getElementsByTagName("algorithm")[0].attribute["name"])

fileLoader = FileSystemLoader("templates")
env = Environment(loader=fileLoader)