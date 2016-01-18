function startWorkflow()
{
    var workflow = actions.create("start-workflow");
    workflow.parameters.workflowName = "activiti$prcMarsHR";
    workflow.parameters["bpm:workflowDescription"] = "Review CV: " + document.name;
    var futureDate = new Date();
    futureDate.setDate(futureDate.getDate()+1);
    workflow.parameters["bpm:workflowDueDate"] = futureDate; 
    return workflow.execute(document);
}

function main()
{
   startWorkflow();
}

main();
