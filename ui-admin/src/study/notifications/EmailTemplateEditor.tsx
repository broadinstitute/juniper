import React, { useRef, useState } from 'react'
import EmailEditor, { EditorRef, EmailEditorProps } from 'react-email-editor'
import { EmailTemplate } from '@juniper/ui-core'
import { Tab, Tabs } from 'react-bootstrap'
import { getMediaBaseUrl } from 'api/api'

export type EmailTemplateEditorProps = {
  emailTemplate: EmailTemplate,
  portalShortcode: string,
  updateEmailTemplate: (emailTemplate: EmailTemplate) => void
}

/** Enables editing an email with design/preview modes */
export default function EmailTemplateEditor({ emailTemplate, updateEmailTemplate, portalShortcode }:
  EmailTemplateEditorProps) {
  const emailEditorRef = useRef<EditorRef>(null)
  // wrapper so that the unlayer event handler always accesses the latest state when updating
  const emailTemplateRef = useRef(emailTemplate)
  emailTemplateRef.current = emailTemplate
  const [activeTab, setActiveTab] = useState<string | null>('designer')

  const replacePlaceholders = (html: string) => {
    return html.replaceAll('${siteMediaBaseUrl}', location.origin + getMediaBaseUrl(portalShortcode))
      // support legacy tempaltes that reference this as siteImageBaseUrl
      .replaceAll('${siteImageBaseUrl}', location.origin + getMediaBaseUrl(portalShortcode))
  }
  const insertPlaceholders = (html: string) => {
    return html.replaceAll(location.origin + getMediaBaseUrl(portalShortcode), '${siteMediaBaseUrl}')
      .replaceAll('${siteImageBaseUrl}', location.origin + getMediaBaseUrl(portalShortcode))
  }

  const onEditorLoaded: EmailEditorProps['onReady'] = unlayer => {
    unlayer.loadDesign({
      // @ts-ignore
      html: replacePlaceholders(emailTemplate.body),
      classic: true
    })
    unlayer.addEventListener('design:updated', () => {
      if (!emailEditorRef.current?.editor) { return }
      emailEditorRef.current.editor.exportHtml(data => {
        updateEmailTemplate({
          ...emailTemplateRef.current,
          body: insertPlaceholders(data.html)
        })
      })
    })
  }

  const templateVersionString = `v${emailTemplate.version}`
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
            options={{ tools: { image: { enabled: false } } }}
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
