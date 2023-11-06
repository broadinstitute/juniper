import React, {useRef, useState} from 'react'
import EmailEditor, {EditorRef, EmailEditorProps, Unlayer} from "react-email-editor";
import {EmailTemplate} from "@juniper/ui-core";
import {Tab, Tabs} from "react-bootstrap";
import {getImageBaseUrl} from "api/api";

export default function EmailTemplateEditor({emailTemplate, updateEmailTemplate, portalShortcode}: {
    emailTemplate: EmailTemplate, portalShortcode: string, updateEmailTemplate: (emailTemplate: EmailTemplate) => void
}) {
    const emailEditorRef = useRef<EditorRef>(null);
    const [activeTab, setActiveTab] = useState<string | null>('designer')

    const replacePlaceholders = (html: string) => {
        return html.replaceAll('${siteImageBaseUrl}', location.origin + getImageBaseUrl(portalShortcode))
    }
    const insertPlaceholders = (html: string) => {
        return html.replaceAll(location.origin + getImageBaseUrl(portalShortcode), '${siteImageBaseUrl}')
    }

    const onEditorLoaded: EmailEditorProps['onReady'] = (unlayer) => {
        unlayer.loadDesign({
            // @ts-ignore
            html: replacePlaceholders(emailTemplate.body),
            classic: true
        })
        unlayer.addEventListener('design:updated', () => {
            if (!emailEditorRef.current?.editor) {return}
            unlayer.loadDesign({
                // @ts-ignore
                html: replacePlaceholders(emailTemplate.body),
                classic: true
            })
            
            // emailEditorRef.current.editor.exportHtml((data) => {
            //     updateEmailTemplate({
            //         ...emailTemplate,
            //         body: insertPlaceholders(data.html)
            //     })
            // });
        })
    }

    let templateVersionString = `v${emailTemplate.version}`
    return <div className="mt-3">
        <div className="d-flex align-items-baseline">
            <h3 className="h6">{emailTemplate.name}</h3>
            <div className="ms-2 text-muted fst-italic">
                ({emailTemplate.stableId} {templateVersionString})
            </div>
        </div>
        <div>
            <label className="form-label">Subject
                <input className="form-control" type="text" size={100} value={emailTemplate.subject}
                       onChange={e => updateEmailTemplate({
                           ...emailTemplate,
                           subject: e.target.value
                       })}/>
            </label>
        </div>
        <div>
            <Tabs
                activeKey={activeTab ?? undefined}
                className="mb-1"
                mountOnEnter
                unmountOnExit
                onSelect={setActiveTab}
            >
                <Tab eventKey="designer" title="Designer">
                    <EmailEditor
                        ref={emailEditorRef}
                        onLoad={onEditorLoaded}
                        onReady={() => 1}
                        options={{tools: {image: {enabled: false}}}}
                    />
                </Tab>
                <Tab eventKey="html" title="Html">
                    <textarea rows={20} cols={100} value={emailTemplate.body}
                              onChange={e => updateEmailTemplate({
                                  ...emailTemplate,
                                  body: e.target.value
                              })}/>

                </Tab>
            </Tabs>
        </div>
    </div>
}