### Populate contexts

Populate configs are classes designed to help with passing populate context down a tree-like 
population process.  

For example, a populate process might start with a FilePopulateContext that points to a root portal.json
file.  From there, the populate process would fork out to files referenced from the portal.json file, including
user and study files.  Those files might in turn fork out to other populate processes.  

The populate contexts allow the current context of the populate (what's the current path, portal, study, etc)
to be easily cloned and passed down to various levels of the tree, so that every populator doesn't have
to take the same 4 arguments.

Populate configs are named after the context they provide, not who they are consumed by.  So, for example, StudyPopulator takes a PortalPopulateContext
as an argument, because the StudyPopulator needs to know what Portal to add the study to.  
