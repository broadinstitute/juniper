import { FormContent } from '@juniper/ui-core'
import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Button } from 'components/forms/Button'
import { faPlus, faTrashAlt } from '@fortawesome/free-solid-svg-icons'

type PageControlsProps = {
    content: FormContent,
    onChange: (newContent: FormContent) => void,
    currentPageNo: number,
    setCurrentPageNo: (pageNo: number) => void
}

export const PageEditControls = ({ content, onChange, currentPageNo, setCurrentPageNo }: PageControlsProps) => {
  const handleCreatePage = () => {
    const newContent = { ...content }
    newContent.pages.splice(currentPageNo + 1, 0, { elements: [] })
    onChange(newContent)
    setCurrentPageNo(currentPageNo + 1)
  }

  const handleDeletePage = () => {
    const newContent = { ...content }
    newContent.pages.splice(currentPageNo, 1)
    onChange(newContent)
    setCurrentPageNo(Math.max(0, currentPageNo - 1))
  }

  return (
    <>
      <Button variant="light" className="border m-1" tooltip={'Create a new page'} onClick={handleCreatePage}>
        <FontAwesomeIcon icon={faPlus}/> Create page
      </Button>
      <Button variant="light" className="border m-1" tooltip={'Delete this page'} onClick={handleDeletePage}>
        <FontAwesomeIcon icon={faTrashAlt}/> Delete page
      </Button>
    </>
  )
}
