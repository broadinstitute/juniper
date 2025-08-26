import React, {
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
import useUpdateEffect from '../../util/useUpdateEffect'

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

  useUpdateEffect(() => {
    if (emailEditorRef.current?.editor && localizedEmailTemplate) {
      emailEditorRef.current.editor.loadDesign({
        // @ts-ignore
        html: replacePlaceholders(localizedEmailTemplate.body),
        classic: true
      })
    }
  }, [selectedLanguage?.languageCode])

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

  const addLocalContent = () => {
    if (!selectedLanguage) { return }

    const defaultContent = emailTemplate.localizedEmailTemplates.find(template =>
      template.language === defaultLanguage.languageCode) ?? {
      subject: '',
      body: ''
    }

    updateEmailTemplate({
      ...emailTemplate,
      id: undefined,
      localizedEmailTemplates: [
        ...emailTemplate.localizedEmailTemplates,
        {
          ...defaultContent,
          language: selectedLanguage.languageCode,
          id: undefined
        }
      ]
    })
  }

  const replacePlaceholders = (html: string) => {
    return html.replaceAll('${siteMediaBaseUrl}', location.origin + getMediaBaseUrl(portalShortcode))
      // support legacy templates that reference this as siteImageBaseUrl
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
      if (!emailEditorRef.current?.editor || !localizedEmailTemplate) { return }
      emailEditorRef.current.editor.exportHtml(data => {
        const matchedTemplate =  emailTemplateRef.current.localizedEmailTemplates.find(template =>
          template.language === localizedEmailTemplate.language)
        const updatedBody = insertPlaceholders(data.html)
        if (matchedTemplate!.body === updatedBody) {
          return
        }
        const updatedTemplates = emailTemplateRef.current.localizedEmailTemplates.map(template =>
          template.language === localizedEmailTemplate.language ? {
            ...localizedEmailTemplate,
            id: undefined,
            body: updatedBody
          } : template
        )
        updateEmailTemplate({
          ...emailTemplateRef.current,
          id: undefined,
          localizedEmailTemplates: updatedTemplates
        })
      })
    })
  }

  const templateVersionString = `v${emailTemplate.version}`
  return <div>
    <div className="d-flex align-items-baseline">
      <h3 className="h6">{emailTemplate.name} template</h3>
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
    { localizedEmailTemplate ? <>
      <div>
        <label className="form-label">Subject
          <input className="form-control" type="text" size={100} value={localizedEmailTemplate.subject}
            onChange={e => {
              const updatedTemplates = emailTemplate.localizedEmailTemplates.map(template =>
                template.language === localizedEmailTemplate.language ? {
                  ...localizedEmailTemplate,
                  id: undefined,
                  subject: e.target.value
                } : template
              )
              updateEmailTemplate({
                ...emailTemplate,
                id: undefined,
                localizedEmailTemplates: updatedTemplates
              })
            }}/>
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
              key={localizedEmailTemplate.language}
              ref={emailEditorRef}
              onLoad={onEditorLoaded}
              options={{ tools: { image: { enabled: false } }, className: 'w-100' }}
            />
            <div className="fst-italic text-muted mx-5">
              If the email editor does not appear here,
              click &quot;Html&quot; above, then &quot;Designer&quot;
            </div>
          </Tab>
          <Tab eventKey="html" title="Html">
            <textarea rows={20} cols={100} value={localizedEmailTemplate.body}
              onChange={e => {
                const updatedTemplates = emailTemplate.localizedEmailTemplates.map(template =>
                  template.language === localizedEmailTemplate.language ? {
                    ...localizedEmailTemplate,
                    id: undefined,
                    body: e.target.value
                  } : template
                )
                updateEmailTemplate({
                  ...emailTemplate,
                  id: undefined,
                  localizedEmailTemplates: updatedTemplates
                })
              }}/>
          </Tab>
        </Tabs>
      </div>
    </> :
      <div className="d-flex flex-column flex-grow-1 mt-2">
        <div className="alert alert-warning" role="alert">
            No content has been configured for this language.
          <button className="btn btn-secondary ms-3" onClick={addLocalContent}>
              Clone from default
          </button>
        </div>
      </div>
    }
  </div>
}
