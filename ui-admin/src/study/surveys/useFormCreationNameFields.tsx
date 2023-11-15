import React, { useState } from 'react'
import { generateStableId } from 'util/pearlSurveyUtils'

/** hook for a form that sets a name and stableId */
export const useFormCreationNameFields = () => {
  const [formName, setFormName] = useState('')
  const [formStableId, setFormStableId] = useState('')
  const [enableAutofillStableId, setEnableAutofillStableId] = useState(true)

  const clearFields = () => {
    setFormName('')
    setFormStableId('')
    setEnableAutofillStableId(true)
  }

  const nameInput = <input type="text" size={50} className="form-control"
    id="inputFormName" value={formName}
    onChange={event => {
      setFormName(event.target.value)
      if (enableAutofillStableId) {
        setFormStableId(generateStableId(event.target.value))
      }
    }}/>

  const stableIdInput = <input type="text" size={50} className="form-control"
    id="inputFormStableId" value={formStableId}
    onChange={event => {
      setFormStableId(event.target.value)
      //Once the user has modified the stable ID on their own,
      // disable autofill in order to prevent overwriting
      setEnableAutofillStableId(false)
    }
    }/>

  return { formName, formStableId, clearFields, nameInput, stableIdInput }
}
