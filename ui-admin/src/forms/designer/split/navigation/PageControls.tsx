import React from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faBackward, faCaretLeft, faCaretRight, faForward } from '@fortawesome/free-solid-svg-icons'
import { FormContent } from '@juniper/ui-core'

type PageControlsProps = {
    currentPageNo: number
    content: FormContent
    setCurrentPageNo: (currentPageNo: number) => void
}

type PageDirection = 'next' | 'previous' | 'first' | 'last'

export const PageControls = ({ currentPageNo, content, setCurrentPageNo }: PageControlsProps) => {
  const handleScrollToTop = () => {
    window.scrollTo(0, 0)
  }

  const handlePageChange = (direction: PageDirection) => {
    switch (direction) {
      case 'next':
        setCurrentPageNo(currentPageNo + 1)
        break
      case 'previous':
        setCurrentPageNo(currentPageNo - 1)
        break
      case 'first':
        setCurrentPageNo(0)
        break
      case 'last':
        setCurrentPageNo(content.pages.length - 1)
        break
    }
    handleScrollToTop()
  }

  const numPages = content.pages.length

  return (
    <div className="d-flex align-items-center">
      <Button variant="light" className="border m-1"
        disabled={currentPageNo === 0}
        onClick={() => handlePageChange('first')}>
        <FontAwesomeIcon icon={faBackward}/>
      </Button>
      <Button variant="light" className="border m-1"
        disabled={currentPageNo === 0}
        onClick={() => handlePageChange('previous')}>
        <FontAwesomeIcon icon={faCaretLeft}/>
      </Button>
      <span>Page {currentPageNo + 1} of {numPages}</span>
      <Button variant="light" className="border m-1"
        disabled={currentPageNo === content.pages.length - 1}
        onClick={() => handlePageChange('next')}>
        <FontAwesomeIcon icon={faCaretRight}/>
      </Button>
      <Button variant="light" className="border m-1"
        disabled={currentPageNo === content.pages.length - 1}
        onClick={() => handlePageChange('last')}>
        <FontAwesomeIcon icon={faForward}/>
      </Button>
    </div>
  )
}
