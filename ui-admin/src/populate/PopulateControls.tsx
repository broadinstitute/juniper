import React, { useState } from 'react'
import { Button } from 'components/forms/Button'
import LoadingSpinner from 'util/LoadingSpinner'

/** renders a boolean radiogroup for setting whether the populate is an overwrite command */
export const OverwriteControl = ({ text, isOverwrite, setIsOverwrite }:
    {text: React.ReactNode, isOverwrite: boolean, setIsOverwrite: (bool: boolean) => void}) => {
  return <div className="py-2">
    <span className="fw-bold">Overwrite</span><br/>
    <label className="me-3">
      <input type="radio" name="overwrite" value="false" checked={!isOverwrite}
        onChange={e => setIsOverwrite(e.target.value === 'true')}
        className="me-1"/> No
    </label>
    <label>
      <input type="radio" name="overwrite" value="true" checked={isOverwrite}
        onChange={e => setIsOverwrite(e.target.value === 'true')}
        className="me-1"/> Yes
    </label>
    <p className="ps-3">{text}</p>
  </div>
}

/**
 * renders a control for specifying a file name --this could be updated to handle the conventions of the various paths
 */
export const useFileNameControl = () => {
  const [fileName, setFileName] = useState('')
  return {
    fileName,
    fileNameControl: <label className="form-label">
            File path (from <code>/populate/src/main/resources/seed</code>)
      <input type="text" value={fileName} className="form-control"
        onChange={e => setFileName(e.target.value)}/>
    </label>
  }
}

/**
 * renders a control for specifying portal shortcodes.  For now it's freetext, to lessen the likelihood of selecting
 * the wrong portal by mistake
 */
export const usePortalShortcodeControl = (initialShortcode: string) => {
  const [portalShortcode, setPortalShortcode] = useState(initialShortcode)
  return {
    portalShortcode,
    shortcodeControl: <label className="form-label">
            Portal shortcode
      <input type="text" value={portalShortcode} className="form-control"
        onChange={e => setPortalShortcode(e.target.value)}/>
    </label>
  }
}

/** button that disables itself and shows a spinner if loading */
export const PopulateButton = ({ onClick, isLoading }: {onClick: () => void, isLoading: boolean}) => {
  return <Button variant="primary" type="button" onClick={onClick} disabled={isLoading}>
    {isLoading ? <LoadingSpinner/> : 'Populate'}
  </Button>
}
