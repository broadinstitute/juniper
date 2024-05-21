import React, { useState } from 'react'
import { generateStableId } from 'util/juniperSurveyUtils'
import { VersionedForm } from '@juniper/ui-core'

/** hook for a form that sets a name and stableId */
export function useFormCreationNameFields<T extends VersionedForm>(form: T, setForm: (form: T) => void) {
  const [enableAutofillStableId, setEnableAutofillStableId] = useState(true)

  const clearFields = () => {
    setForm({
      ...form,
      name: '',
      stableId: ''
    })
    setEnableAutofillStableId(true)
  }

  const NameInput = <input type="text" size={50} className="form-control"
    id="inputFormName" value={form.name}
    onChange={event => {
      setForm({
        ...form,
        name: event.target.value,
        stableId: enableAutofillStableId ? generateStableId(event.target.value) : form.stableId
      })
    }}/>

  const StableIdInput = <input type="text" size={50} className="form-control"
    id="inputFormStableId" value={form.stableId}
    onChange={event => {
      setForm({ ...form, stableId: event.target.value })
      //Once the user has modified the stable ID on their own,
      // disable autofill in order to prevent overwriting
      setEnableAutofillStableId(false)
    }
    }/>

  return { clearFields, NameInput, StableIdInput }
}
