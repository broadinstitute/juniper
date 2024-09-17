import React from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faBackward, faCaretLeft, faCaretRight, faForward } from '@fortawesome/free-solid-svg-icons'
import { FormContent } from '@juniper/ui-core'
import { handleScrollToTop } from '../../utils/formDesignerUtils'

type PageControlsProps = {
    currentPageNo: number
    content: FormContent
    setCurrentPageNo: (currentPageNo: number) => void
}

type PageDirection = 'next' | 'previous' | 'first' | 'last'

export const PageNavigationControls = ({ currentPageNo, content, setCurrentPageNo }: PageControlsProps) => {
  const handlePageChange = (direction: PageDirection) => {
    handleScrollToTop()
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
  }

  const numPages = content.pages.length

  return (
    <div className="d-flex align-items-center">
      <Button variant="light" className="border m-1"
        disabled={currentPageNo === 0}
        aria-label={'Go to first page'}
        tooltip={'Go to first page'}
        onClick={() => handlePageChange('first')}>
        <FontAwesomeIcon icon={faBackward}/>
      </Button>
      <Button variant="light" className="border m-1"
        disabled={currentPageNo === 0}
        aria-label={'Go to previous page'}
        tooltip={'Go to previous page'}
        onClick={() => handlePageChange('previous')}>
        <FontAwesomeIcon icon={faCaretLeft}/>
      </Button>
      <span className={'px-1'}>Page {currentPageNo + 1} of {numPages}</span>
      <Button variant="light" className="border m-1"
        disabled={currentPageNo === content.pages.length - 1}
        aria-label={'Go to next page'}
        tooltip={'Go to next page'}
        onClick={() => handlePageChange('next')}>
        <FontAwesomeIcon icon={faCaretRight}/>
      </Button>
      <Button variant="light" className="border m-1"
        disabled={currentPageNo === content.pages.length - 1}
        aria-label={'Go to last page'}
        tooltip={'Go to last page'}
        onClick={() => handlePageChange('last')}>
        <FontAwesomeIcon icon={faForward}/>
      </Button>
    </div>
  )
}
