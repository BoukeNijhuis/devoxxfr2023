import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.SuspendPolicy.NONE
import com.intellij.xdebugger.impl.XDebuggerUtilImpl
import com.intellij.xdebugger.impl.XSourcePositionImpl
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil
import liveplugin.currentFile
import liveplugin.editor
import liveplugin.registerAction
import liveplugin.show

registerAction(
    id = "Log Selection with Breakpoint", keyStroke = "ctrl meta F8"
) { event: com.intellij.openapi.actionSystem.AnActionEvent ->
    // get current project and file
    val project = event.project ?: return@registerAction
    val currentFile = project.currentFile ?: return@registerAction
    val editor = event.editor ?: return@registerAction

    // determine the position variables
    val offset = editor.caretModel.offset
    val currentPosition = XSourcePositionImpl.createByOffset(currentFile, offset) as XSourcePosition
    val nextLinePosition = XSourcePositionImpl.create(currentFile, currentPosition.line + 1)

    // check the selection and determine the breakpoint position
    val selectedText = editor.selectionModel.selectedText
    var position = currentPosition;
    if (selectedText != null) {
        position = nextLinePosition
    }

    // increase the line number if the determined position does not support a breakpoint
    while (!XDebuggerUtilImpl().canPutBreakpointAt(project, currentFile, position.line)) {
        position = XSourcePositionImpl.create(currentFile, position.line + 1)
    }

    // always toggle (even if there is no selection)
    val types = XBreakpointUtil.getAvailableLineBreakpointTypes(project, position, editor)
    val breakpoint =
        XDebuggerUtilImpl.toggleAndReturnLineBreakpoint(project, types, position, false, editor, true)

    // update the breakpoint with the log expression
    if (selectedText != null) {
        breakpoint.then {
            it.suspendPolicy = NONE
            it.logExpression = "\"$selectedText = [\" + $selectedText + \"]\""
        }
    }
}

if (!isIdeStartup) show("Loaded 'Log Selection with Breakpoint' action<br/>Use 'Ctrl+Command+F8' to run it")
