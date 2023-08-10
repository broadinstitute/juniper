import { VersionedForm } from '@juniper/ui-core'

export type FormDraft = {
  content: string
  date: number
}

/** returns a form draft key for local storage */
export function getFormDraftKey({ form }: { form: VersionedForm }) {
  return `surveyDraft_${form.id}_${form.version}`
}

/** returns a form draft from local storage, if there is one */
export function getDraft({ formDraftKey }: { formDraftKey: string }): FormDraft | undefined {
  const draft = localStorage.getItem(formDraftKey)
  if (!draft) {
    return undefined
  } else {
    const draftParsed: FormDraft = JSON.parse(draft)
    return draftParsed
  }
}

/** saves a form draft to local storage with the current timestamp, if there is one */
export function saveDraft({ formDraftKey, draft, setSavingDraft }: {
  formDraftKey: string,
  draft: FormDraft
  setSavingDraft: (saving: boolean) => void
}) {
  setSavingDraft(true)
  localStorage.setItem(formDraftKey, JSON.stringify(draft))
  //Saving a draft happens so quickly that the "Saving draft..." message isn't even visible to the user.
  //Set a timeout to show it for 2 seconds so the user knows that their drafts are being saved.
  setTimeout(() => {
    setSavingDraft(false)
  }, 2000)
}

/** deletes a form draft from local storage */
export function deleteDraft({ formDraftKey }: { formDraftKey: string }) {
  localStorage.removeItem(formDraftKey)
}
