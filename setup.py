from setuptools import setup

with open("README.md", "r") as fh:
    long_description = fh.read()

setup(
    name='cognipy',
    version='0.1.5',
    author="Cognitum Services S.A.",
    author_email="support@cognitum.eu",
    description="Semantic Tech Reasoner and Editor",
    long_description=open("README.md").read(),
    long_description_content_type="text/markdown",
    url="https://github.com/cognitum-octopus/cognipy",
    packages=['cognipy'],
    include_package_data = True,
    install_requires = ['pandas','pydot','ipywidgets',],
    classifiers=[
        "Development Status :: 3 - Alpha",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: Apache Software License",
        "Programming Language :: C#",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.5",
        "Programming Language :: Python :: 3.6",
        "Programming Language :: Python :: 3.7",
        "Operating System :: Microsoft :: Windows",
        "Operating System :: POSIX :: Linux",
        "Operating System :: MacOS :: MacOS X",
    ],
 )
