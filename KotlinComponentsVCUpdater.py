import os
import re

#  Run this file inside of your app submodule to update most things that
#  we had to update in the newest version of Kotlin Components to allow for
#  previewing layouts

rootdir = os.path.dirname(os.path.abspath(__file__))

kt_replacements = [
    # (re.compile(r'button\((\w)'), r'button(text = \1')
]
kt_plain_replacements = [
	("AnkoContext<VCActivity>", "AnkoContext<VCContext>"),
	("import com.lightningkite.kotlin.anko.viewcontrollers.implementations.VCActivity", "import com.lightningkite.kotlin.anko.viewcontrollers.VCContext"),
	("viewContainer(", "viewContainer(ui.owner, "),
	("viewController(", "viewController(ui.owner, "),
	("embedViewContainer(", "embedViewContainer(ui.owner, "),
	("ui.owner.resources", "ui.ctx.resources"),
	("ui.owner.contentResolver", "ui.ctx.contentResolver"),
	("ui.owner.browse", "ui.ctx.browse"),
	("ui.owner.email", "ui.ctx.email"),
	("ui.owner.defaultSharedPreferences", "ui.ctx.defaultSharedPreferences"),
	("ui.owner.infoDialog", "ui.ctx.infoDialog"),
	("ui.owner.dialog", "ui.ctx.dialog"),
	("ui.owner.customDialog", "ui.ctx.customDialog"),
	("ui.owner.standardDialog", "ui.ctx.standardDialog"),
	("ui.owner.confirmationDialog", "ui.ctx.confirmationDialog"),
	("ui.owner.timePicker", "ui.ctx.timePicker"),
	("ui.owner.datePicker", "ui.ctx.datePicker"),
	("ui.owner.selector", "ui.ctx.selector"),
	(":VCActivity", ":VCContext"),
	(": VCActivity", ": VCContext"),
	("as VCActivity", "as VCContext"),
	("as? VCActivity", "as? VCContext")
]

def do_replace_kt(text):
    for pattern, replace in kt_plain_replacements:
        text = text.replace(pattern, replace)
    for pattern, replace in kt_replacements:
        text = pattern.sub(replace, text)
    return text


def do_replace_on_file(path):
    if path.endswith('.kt'):
        text = open(path).read()
        afterText = do_replace_kt(text)
        open(path, 'w').write(afterText)

for subdir, dirs, files in os.walk(rootdir):
    for file in files:
        path = os.path.join(subdir, file)
        if 'build' in path:
            continue
        print path
        do_replace_on_file(path)
