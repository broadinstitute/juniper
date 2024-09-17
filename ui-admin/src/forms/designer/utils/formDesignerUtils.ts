export const handleScrollToTop = () => {
  window.scrollTo(0, 0)
}

export const scrollToElement = (path: string, setCurrentPageNo: (pageNo: number) => void) => {
  //parses path from url params, i.e.: selectedElementPath=pages%5B0%5D.elements%5B0%5D
  const pathParts = path.split('.')
  const pageElement = pathParts[0]
  const pageElementIndex = parseInt(pageElement.replace('pages[', '').replace(/\]/g, ''))
  setCurrentPageNo(pageElementIndex)

  if (pathParts.length === 1) {
    handleScrollToTop()
  }

  if (pathParts.length > 1) {
    const elementIndex = parseInt(pathParts[1].replace('elements[', '').replace(/\]/g, ''))
    const element = document.getElementById(`element[${elementIndex}]`)
    //we need to wait for the element to be rendered so we can scroll to it
    if (element) {
      setTimeout(() => element.scrollIntoView({ behavior: 'auto' }), 100)
    }
  }
}
