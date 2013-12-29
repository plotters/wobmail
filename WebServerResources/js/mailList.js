var classAttrName;
if (document.all)
	classAttrName = "className";
else
	classAttrName = "class";

function addOnLoadEvent(func)
{
	var oldonload = window.onload;
	if (typeof window.onload != 'function')
		window.onload = func;
	else
	{
		window.onload = function()
		{
			oldonload();
			func();
		}
	}
}

function registerMessageListCheckboxes()
{
	// get main list table element
	var listTable = document.getElementById("XWMmessageList");

	if (!listTable)
		return;

	// Get all checkboxes and add an onclick
	var checkboxes = listTable.getElementsByTagName("input");
	for (var i=0; i<checkboxes.length; i++)
	{
		var checkbox = checkboxes[i];
		//if (checkbox.getAttribute("isHighlightable") == "yes")
		//{
			// set the onclick event to toggle the class of the corresponding row the checkbox identified by its spanId
			checkbox.onclick = function() {
				toggleMessageListRowClassForCheckbox(this);
			}
		//}
	}
}

function toggleMessageListRowClassForCheckbox(object)
{
	if (!object)
		return;

	var row = object.parentNode.parentNode;

	if (object.checked)
		row.setAttribute(classAttrName, 'XWMlistRowLightSelected');
	else
		row.setAttribute(classAttrName, 'XWMlistRowLight');
}

addOnLoadEvent(registerMessageListCheckboxes);
