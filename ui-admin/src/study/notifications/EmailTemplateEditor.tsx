import React, {
  useEffect,
  useRef,
  useState
} from 'react'
import EmailEditor, {
  EditorRef,
  EmailEditorProps
} from 'react-email-editor'
import {
  EmailTemplate,
  PortalEnvironmentLanguage
} from '@juniper/ui-core'
import {
  Tab,
  Tabs
} from 'react-bootstrap'
import { getMediaBaseUrl } from 'api/api'
import { usePortalLanguage } from 'portal/languages/usePortalLanguage'
import useReactSingleSelect from 'util/react-select-utils'
import Select from 'react-select'

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
  const { defaultLanguage, supportedLanguages } = usePortalLanguage()
  const [selectedLanguage, setSelectedLanguage] = useState<PortalEnvironmentLanguage | undefined>(defaultLanguage)
  const localizedEmailTemplate = emailTemplate.localizedEmailTemplates.find(template =>
    template.language === selectedLanguage?.languageCode)

  useEffect(() => {
    if (emailEditorRef.current?.editor && localizedEmailTemplate) {
      emailEditorRef.current.editor.loadDesign({
        // @ts-ignore
        html: replacePlaceholders(localizedEmailTemplate.body),
        classic: true
      })
    }
  }, [localizedEmailTemplate])

  const {
    onChange: languageOnChange, options: languageOptions,
    selectedOption: selectedLanguageOption, selectInputId: selectLanguageInputId
  } =
      useReactSingleSelect(
        supportedLanguages,
        (language: PortalEnvironmentLanguage) => ({ label: language.languageName, value: language }),
        setSelectedLanguage,
        selectedLanguage
      )

  if (!localizedEmailTemplate) {
    return <div>no localized template found for {defaultLanguage.languageCode}</div>
  }

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
      html: replacePlaceholders(localizedEmailTemplate.body),
      classic: true
    })
    unlayer.addEventListener('design:updated', () => {
      if (!emailEditorRef.current?.editor) { return }
      emailEditorRef.current.editor.exportHtml(data => {
        updateEmailTemplate({
          ...emailTemplateRef.current,
          localizedEmailTemplates: [{
            ...localizedEmailTemplate,
            id: undefined,
            body: insertPlaceholders(data.html)
          }]
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
    { supportedLanguages.length > 1 && <div style={{ width: 200 }}>
      <label className="form-label">Language
        <Select options={languageOptions} value={selectedLanguageOption} inputId={selectLanguageInputId}
          aria-label={'Select a language'}
          onChange={e => {
            languageOnChange(e)
          }}/>
      </label>
    </div> }
    <div>
      <label className="form-label">Subject
        <input className="form-control" type="text" size={100} value={localizedEmailTemplate.subject}
          onChange={e => updateEmailTemplate({
            ...emailTemplate,
            localizedEmailTemplates: [{
              ...localizedEmailTemplate,
              id: undefined,
              subject: e.target.value
            }]
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
            options={{ tools: { image: { enabled: false } }, className: 'w-100' }}
            style={{ maxWidth: '0px', width: '0px' }}
          />
        </Tab>
        <Tab eventKey="html" title="Html">
          <textarea rows={20} cols={100} value={localizedEmailTemplate.body}
            onChange={e => updateEmailTemplate({
              ...emailTemplate,
              localizedEmailTemplates: [{
                ...localizedEmailTemplate,
                id: undefined,
                body: e.target.value
              }]
            })}/>

        </Tab>
      </Tabs>
    </div>
  </div>
}
