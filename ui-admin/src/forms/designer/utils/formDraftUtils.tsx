type Draft = {
  content: string
  date: number
}

/** returns a form draft from local storage, if there is one */
export function getDraft({ formDraftKey }: { formDraftKey: string }): Draft | undefined {
  const draft = localStorage.getItem(formDraftKey)
  if (!draft) {
    return undefined
  } else {
    const draftParsed: Draft = JSON.parse(draft)
    return draftParsed
  }
}

/** saves a form draft to local storage with the current timestamp, if there is one */
export function saveDraft({ formDraftKey, content, setSavingDraft }: {
  formDraftKey: string,
  content: string
  setSavingDraft: (saving: boolean) => void
}) {
  const date = Date.now()
  setSavingDraft(true)
  localStorage.setItem(formDraftKey, JSON.stringify({ content, date }))
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
