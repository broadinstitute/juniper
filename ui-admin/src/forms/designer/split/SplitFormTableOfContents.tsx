import React from 'react'

import { FormContent, FormElement } from '@juniper/ui-core'

import { Tree, TreeItemT } from 'components/Tree'

type SplitFormContentTableOfContentsTreeItem = TreeItemT<{
  /** If this item can be selected. */
  isSelectable: boolean

  /** Path to the form element associated with this tree item in object notation. For example, 'pages[0].elements[1]' */
  path: string
}>

/** Recursive helper for getTableOfContentsTree. */
const getSplitTableOfContentsTreeHelper = (parentPath: string) => {
  return (formElement: FormElement, elementIndex: number): SplitFormContentTableOfContentsTreeItem => {
    if ('type' in formElement && formElement.type === 'panel') {
      return {
        label: <span>Panel <span className="fw-light fst-italic">({formElement.elements.length} items)</span></span>,
        data: {
          isSelectable: true,
          path: `${parentPath}[${elementIndex}]`
        },
        children: formElement.elements.map(getSplitTableOfContentsTreeHelper(`${parentPath}[${elementIndex}].elements`))
      }
    } else {
      return {
        label: formElement.name,
        data: {
          isSelectable: true,
          path: `${parentPath}[${elementIndex}]`
        }
      }
    }
  }
}

/** Convert a FormContent object into a TreeItemT to render the table of contents as a string. */
export const getSplitTableOfContentsTree = (formContent: FormContent): SplitFormContentTableOfContentsTreeItem => {
  return {
    data: {
      isSelectable: true,
      path: 'pages'
    },
    children: (formContent.pages || []).map((page, pageIndex) => ({
      label: `Page ${pageIndex + 1}`,
      data: {
        isSelectable: true,
        path: `pages[${pageIndex}]`
      },
      children: page.elements.map(getSplitTableOfContentsTreeHelper(`pages[${pageIndex}].elements`))
    }))
  }
}

type SplitFormTableOfContentsProps = {
    formContent: FormContent
    selectedElementPath: string | undefined
    onSelectElement: (path: string) => void
}

/** Render a table of contents for a form. */
export const SplitFormTableOfContents = (props: SplitFormTableOfContentsProps) => {
  const { formContent, selectedElementPath, onSelectElement } = props

  return (
    <Tree
      id="form-table-of-contents"
      isItemSelected={item => item.data.path === selectedElementPath}
      label="Table of contents"
      rootItem={getSplitTableOfContentsTree(formContent)}
      onClickItem={item => {
        const { isSelectable, path } = item.data
        if (isSelectable) {
          onSelectElement(path)
        }
      }}
    />
  )
}
