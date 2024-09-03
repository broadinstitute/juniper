import React, {useState} from "react";
import {render} from "@testing-library/react";
import {PageElementList} from "forms/designer/PageElementList";
import {SplitCalculatedValueDesigner} from "forms/designer/SplitCalculatedValueDesigner";
import {FormContent} from "@juniper/ui-core";

const TestCalculatedValueDesignerWrapper({initialContent}:{initialContent: FormContent}) => {
  const [content, setContent] = useState(initialContent)
  return <SplitCalculatedValueDesigner content={content} onChange={setContent}/>
}

test('SplitCalculatedValueDesigner', () => {
  it('should render', () => {

    render(<TestCalculatedValueDesignerWrapper/>)

  })
})
