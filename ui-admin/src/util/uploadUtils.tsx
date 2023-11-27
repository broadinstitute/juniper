import React, { ChangeEvent, useRef, useState } from 'react'
import { Button } from '../components/forms/Button'

// type for file chooser events -- see https://github.com/microsoft/TypeScript/issues/31816
type FileEvent = ChangeEvent<HTMLInputElement> & {
    target: EventTarget & { files: FileList };
};

/** hook for a file chooser that uses our theming.  It's impossible to style the system file chooser, so the
 * recommended path is to hide it and render our own.  */
export const useFileUploadButton = (onFileChange: (file: File) => void) => {
  const hiddenFileInput = useRef<HTMLInputElement>(null)
  const [file, setFile] = useState<File>()

  const handleFileChange = (event: FileEvent) => {
    setFile(event.target.files[0])
    onFileChange(event.target.files[0])
  }

  const handleClick = () => {
    if (hiddenFileInput.current) {
      hiddenFileInput.current.click()
    }
  }

  return {
    FileChooser: <span>
      <Button variant="primary" outline={true} onClick={handleClick}>
                    Choose file
      </Button>
      <input type="file" data-testid="fileInput"
        onChange={handleFileChange} ref={hiddenFileInput} style={{ display: 'none' }}/>
    </span>,
    file
  }
}
