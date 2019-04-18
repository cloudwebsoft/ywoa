	var bkColor ="#EFFFB2";
	var diffBkColor ="#F9ADAD";

	function CompareById(v_nodeID1, v_nodeID2)
	{
		var LeftContentObj = o(v_nodeID1);
		var RightContentObj = o(v_nodeID2);

		var LeftNodesList = new Array;
		LeftNodesList = getTextNodes(LeftContentObj);

		var RightNodesList = new Array;
		RightNodesList = getTextNodes(RightContentObj);

		CompareNodes(LeftNodesList, RightNodesList);
	}
	function Equal(v_node1, v_node2)
	{
		if (v_node1.data == v_node2.data)
		 return true;
		return false;
	}
	function CompareText(v_LeftList, v_RightList)
	{
		var leftLen = v_LeftList.length;
		var rightLen = v_RightList.length;
		if (rightLen == leftLen && 1 == rightLen)
		{	
			if (v_LeftList[0].data.length==v_RightList[0].data.length && 1==v_RightList[0].data.length )
				return;
		}
		if (0 == leftLen)
		{
			for (var i = 0; i < rightLen; i++)
				paintNode(v_RightList[i], diffBkColor);
		}
		else if (0 == rightLen)
		{
			for (var i = 0; i < leftLen; i++)
				paintNode(v_LeftList[i], diffBkColor);
		}
		else
		{
			//--------------------compare text--------------------
			var LeftTextList = new Array;
			LeftTextList = GetCharList(v_LeftList);
			var RightTextList = new Array;
			RightTextList = GetCharList(v_RightList);

			var ResultList = new Array;
			ResultList = CompareChars(LeftTextList, RightTextList);	
			if (0 != ResultList[0].length || 0 != ResultList[1].length)
			{
				for (var i = 0; i < v_LeftList.length; i++)
					v_LeftList[i] = paintNode(v_LeftList[i], bkColor);
					
				for (var i = 0; i < v_RightList.length; i++)
					v_RightList[i] = paintNode(v_RightList[i], bkColor);					
			}	

			Display(ResultList[0], v_LeftList);
			Display(ResultList[1], v_RightList);
			//------------------------END-------------------------
		}			
	}
	
	/*将text节点中的文本转化成char，放入数组*/
	function GetCharList(v_nodeList)
	{
		var CharList = new Array;
		for (var i = 0; i < v_nodeList.length; i++)
		{
			var text = v_nodeList[i].data;
			for (var j = 0; j < text.length; j++)
			{
				CharList[CharList.length] = text.charAt(j);
			}
		}
		return CharList;
	}
	/*显示比较结果*/
	function Display(v_posList, v_nodeList)
	{
		var NodeTextList = new Array;			
		if (0 == v_posList.length)
			return;
			
		for (var i = 0; i < v_nodeList.length; i++)
		{
			NodeTextList.length = 0;
			/*取文本分割点放入 splitPos[] 数组 */
			var splitPos = new Array;
			if (1 == v_nodeList.length)
				splitPos = v_posList;
			else
			{	
				var begin = 0;
				if (i > 0)
				{
					for (var k = 0; k < i; k++)
						begin += v_nodeList[k].data.length;
				}
				splitPos = GetSplitPosList(v_posList, v_nodeList[i].data.length, begin); 
 			}		
					
			NodeTextList = SplitTexe(splitPos, v_nodeList[i].data); 
			PaintText(v_nodeList[i], NodeTextList, diffBkColor);
		}	
	}
	
	/*取文本分割点放入 splitPos[] 数组 */	
	function GetSplitPosList(v_list, v_len, v_begin)
	{
		var splitPos = new Array;
		var preLen = 0;
		for (var j = 0; j < v_list.length/2; j++)
		{
			if (v_list[2*j] <= v_begin)
			{
				if (v_list[2*j + 1] < v_begin)
					continue;
				else if (v_list[2*j + 1] > v_begin + v_len - 1)
				{
					splitPos[splitPos.length] = v_list[2*j] - v_begin + preLen;
					splitPos[splitPos.length] = v_len - 1 + preLen;
					break;
				}	
				else
				{
					splitPos[splitPos.length] = preLen;
					splitPos[splitPos.length] = v_list[2*j + 1] - v_begin + preLen;
					preLen = preLen + v_list[2*j + 1] - v_begin +1;
					v_len = v_len - v_list[2*j + 1] + v_begin -1;
					if (0 == v_len)
					  break;
					v_begin = v_list[2*j + 1] + 1;
				}			
			}
			else
			{
				if (v_list[2*j] > v_begin + v_len - 1)
					break;
				else if (v_list[2*j + 1] >= v_begin + v_len - 1)
				{
					splitPos[splitPos.length] = v_list[2*j] - v_begin + preLen;
					splitPos[splitPos.length] = v_len - 1 + preLen;
					break;							
				}
				else
				{
					splitPos[splitPos.length] = v_list[2*j] - v_begin + preLen;
					splitPos[splitPos.length] = v_list[2*j + 1] - v_begin + preLen;		
					preLen = preLen + v_list[2*j + 1] - v_begin +1;
					v_len = v_len - v_list[2*j + 1] + v_begin -1;
					if (0 == v_len)
					  break;
					v_begin = v_list[2*j + 1] + 1;
				}		
			}	
		}

		return splitPos;
	}
	/*根据分割点，分割文本放入数组*/
	function SplitTexe(v_splitPosList, v_text)
	{
		var NodeTextList = new Array;
		var begin = 0;
		for (var j = 0; j < v_splitPosList.length/2; j++)
		{
			if (v_splitPosList[2*j] > 0)
				NodeTextList[2*j] = v_text.substring(begin, v_splitPosList[2*j]);
			else
				NodeTextList[2*j] = null;	
			NodeTextList[2*j + 1] = v_text.substring(v_splitPosList[2*j], v_splitPosList[2*j + 1] + 1);

			begin = v_splitPosList[2*j + 1] + 1;	
		}
		if (begin < v_text.length)
		{
			NodeTextList[NodeTextList.length] = v_text.substring(begin, v_text.length + 1);
			NodeTextList[NodeTextList.length] = null;
		}
		return NodeTextList;
	}
	
	function PaintText(v_node, v_textList, v_color)
	{
		var newNodeTextList = new Array;
		for (var i = 0; i < v_textList.length; i=i+2)
		{/*处理相同部分*/
			if (v_textList[i] == null)
				newNodeTextList[i] = null;
			else
				newNodeTextList[i] = document.createTextNode(v_textList[i]);
			
			/*处理不相同部分*/	
			if (v_textList[i+1] == null)
				newNodeTextList[i+1] = null;
			else
			{	
				var temp = document.createTextNode(v_textList[i+1]);
				var newNode = document.createElement('SPAN');
				newNode.style.background = v_color;
				newNode.appendChild(temp);
				newNodeTextList[i+1] = newNode;
			}
			
		}

		var fatherNode = v_node.parentNode;
		fatherNode.removeChild(v_node);
		for (var i = 0; i < newNodeTextList.length; i++)
		{
			if (newNodeTextList[i] != null)
				fatherNode.appendChild(newNodeTextList[i]);
		}
	}
	
	function CompareChars(v_LeftCharsList, v_RightCharsList)
	{
		var ResultList = new Array;
		var RightDiffList = new Array;
		var LeftDiffList = new Array;
				
		var RightIndex =0;
		var LeftIndex =0;
		var nLeftCharsListLen = v_LeftCharsList.length;
		var nRightCharsListLen = v_RightCharsList.length;
		if (nLeftCharsListLen == nRightCharsListLen)
		{
			for (var i = 0; i < nLeftCharsListLen; i++)
			{
				if (v_LeftCharsList[i] == v_RightCharsList[i])
				{
					if (i - LeftIndex != 0)
					{
						LeftDiffList[LeftDiffList.length] = LeftIndex;
						LeftDiffList[LeftDiffList.length] = i - 1;
						
						RightDiffList[RightDiffList.length] = RightIndex;
						RightDiffList[RightDiffList.length] = i - 1;
					}
					LeftIndex = i + 1;
					RightIndex = i + 1;
				}
			}
			if (LeftIndex != nLeftCharsListLen )
			{
				LeftDiffList[LeftDiffList.length] = LeftIndex;
				LeftDiffList[LeftDiffList.length] = nLeftCharsListLen - 1;
				
				RightDiffList[RightDiffList.length] = RightIndex;
				RightDiffList[RightDiffList.length] = nLeftCharsListLen - 1;
			}
			ResultList[0] = LeftDiffList;
			ResultList[1] = RightDiffList;
			return ResultList;
		}	
		
		while(LeftIndex < nLeftCharsListLen || RightIndex < nRightCharsListLen)
		{
			///left is end
			if (LeftIndex == nLeftCharsListLen)
			{
				RightDiffList[RightDiffList.length] = RightIndex;
				RightDiffList[RightDiffList.length] = nRightCharsListLen - 1;
				break;
			}	
			///right is end
			if (RightIndex == nRightCharsListLen)
			{
				LeftDiffList[LeftDiffList.length] = LeftIndex;
				LeftDiffList[LeftDiffList.length] = nLeftCharsListLen - 1;
				break;
			}	
			
			if (v_LeftCharsList[LeftIndex] == v_RightCharsList[RightIndex])
			{
				///same
				LeftIndex++;
				RightIndex++;
			}	
			else
			{
				var i = 0;
				for (i = RightIndex + 1; i < nRightCharsListLen; i++)
				{
					if (v_LeftCharsList[LeftIndex] == v_RightCharsList[i])
					{
						RightDiffList[RightDiffList.length] = RightIndex;
						RightDiffList[RightDiffList.length] = i - 1;
				
						LeftIndex++;
						RightIndex = i + 1;
						break;
					}		
				}
				
				if (i == nRightCharsListLen)
				{///right is over
					i = 0;
					for (i = LeftIndex + 1; i < nLeftCharsListLen; i++)
					{
						if (v_RightCharsList[RightIndex] == v_LeftCharsList[i])
						{
							LeftDiffList[LeftDiffList.length] = LeftIndex;
							LeftDiffList[LeftDiffList.length] = i - 1;
							RightIndex++;
							LeftIndex = i + 1;
							break;
						}		
					}
					if (i == nLeftCharsListLen)
					{///left is over, both is diff
						LeftDiffList[LeftDiffList.length] = LeftIndex;
						LeftDiffList[LeftDiffList.length] = LeftIndex;
						RightDiffList[RightDiffList.length] = RightIndex;
						RightDiffList[RightDiffList.length] = RightIndex;
						LeftIndex++;
						RightIndex++;
					}
				}//if (i == nRightCharsListLen)
			}//else
		}//while
		ResultList[0] = LeftDiffList; 
		ResultList[1] = RightDiffList;
		return ResultList;
	}

	function CompareNodes(v_LeftNodesList, v_RightNodesList)
	{
		var PreRightIndex =-1;
		var PreLeftIndex =-1;
		var RightIndex =0;		
		var LeftIndex =0;
		var nLeftNodesListLen = v_LeftNodesList.length;
		var nRightNodesListLen = v_RightNodesList.length;
		while(LeftIndex <= nLeftNodesListLen || RightIndex <= nRightNodesListLen)
		{
			///left is end
			if (LeftIndex == nLeftNodesListLen)
			{
				CompareText(getNodeList(v_LeftNodesList, PreLeftIndex + 1, LeftIndex - PreLeftIndex - 1), getNodeList(v_RightNodesList, PreRightIndex + 1, RightIndex - PreRightIndex - 1));
				for (var j = RightIndex; j < nRightNodesListLen; j++)
					v_RightNodesList[j] = paintNode(v_RightNodesList[j], diffBkColor);
				break;
			}	
			///right is end
			if (RightIndex == nRightNodesListLen)
			{
				CompareText(getNodeList(v_LeftNodesList, PreLeftIndex + 1, LeftIndex - PreLeftIndex - 1), getNodeList(v_RightNodesList, PreRightIndex + 1, RightIndex - PreRightIndex - 1));
				for (var j = LeftIndex; j < nLeftNodesListLen; j++)
					v_LeftNodesList[j] = paintNode(v_LeftNodesList[j], diffBkColor);
				break;
			}	
			
			if (Equal(v_LeftNodesList[LeftIndex] , v_RightNodesList[RightIndex]))
			{
				if (v_LeftNodesList[LeftIndex].data.length > 1) 
				{
					///same
					CompareText(getNodeList(v_LeftNodesList, PreLeftIndex + 1, LeftIndex - PreLeftIndex - 1), getNodeList(v_RightNodesList, PreRightIndex + 1, RightIndex - PreRightIndex - 1));
					PreLeftIndex = LeftIndex;
					PreRightIndex = RightIndex;
				}
				else if (v_LeftNodesList[LeftIndex].data.length == 1 && v_LeftNodesList[LeftIndex].data.charCodeAt(0) > 127)
				{
					if(v_LeftNodesList[LeftIndex].data.charCodeAt(0)!= 160){
					PreLeftIndex = LeftIndex;
					PreRightIndex = RightIndex;
					}
				}
				LeftIndex++;
				RightIndex++;
			}	
			else
			{
				var i = 0;
				for (i = RightIndex + 1; i < nRightNodesListLen; i++)
				{
					if (Equal(v_LeftNodesList[LeftIndex], v_RightNodesList[i]))
					{
						if (v_LeftNodesList[LeftIndex].data.length > 1) 
						{			
							CompareText(getNodeList(v_LeftNodesList, PreLeftIndex + 1, LeftIndex - PreLeftIndex - 1), getNodeList(v_RightNodesList, PreRightIndex + 1, i - PreRightIndex - 1));

							PreLeftIndex = LeftIndex;
							PreRightIndex = i;
							LeftIndex++;
							RightIndex = i + 1;
						}
						else
						{	
							if (v_LeftNodesList[LeftIndex].data.length == 1 && v_LeftNodesList[LeftIndex].data.charCodeAt(0) > 127)
							{
								if(v_LeftNodesList[LeftIndex].data.charCodeAt(0)!= 160){
								PreLeftIndex = LeftIndex;
								PreRightIndex = RightIndex;
								}
							}							
							RightIndex++;
							LeftIndex++;
						}
						break;
					}		
				}
				
				if (i == nRightNodesListLen)
				{///right is over
					i = 0;
					for (i = LeftIndex + 1; i < nLeftNodesListLen; i++)
					{
						if (Equal(v_RightNodesList[RightIndex] , v_LeftNodesList[i]))
						{
							if (v_LeftNodesList[i].data.length > 1)
							{																
								CompareText(getNodeList(v_LeftNodesList, PreLeftIndex + 1, i - PreLeftIndex - 1), getNodeList(v_RightNodesList, PreRightIndex + 1, RightIndex - PreRightIndex - 1));
								PreLeftIndex = i;
								PreRightIndex = RightIndex;
								RightIndex++;
								LeftIndex = i + 1;								
							}
							else
							{	
								if (v_LeftNodesList[LeftIndex].data.length == 1 && v_LeftNodesList[LeftIndex].data.charCodeAt(0) > 127)
								{
									if(v_LeftNodesList[LeftIndex].data.charCodeAt(0)!= 160){
									PreLeftIndex = LeftIndex;
									PreRightIndex = RightIndex;
									}
								}									
								RightIndex++;
								LeftIndex++;
							}
							break;
						}		
					}
					if (i == nLeftNodesListLen)
					{///left is over, both is diff
						LeftIndex++;
						RightIndex++;
					}
				}//if (i == nRightNodesListLen)
			}//else
		}//while
	}
	
	function getNodeList(v_nodesList, v_begin, v_len)
	{
		var List = new Array;
		for (var j = 0; j < v_len; j++)
		{
			List[j] = v_nodesList[v_begin + j];
		}
		return List;		
	}
	
	function paintNode(v_node, v_color)
	{
		var newNode = document.createElement('SPAN');
		var colorname = v_color;
		newNode.style.background = v_color;

		var childNodeCopy = v_node.cloneNode(true);
		newNode.appendChild(childNodeCopy);

		var fatherNode = v_node.parentNode;
		if(fatherNode!=null)
		fatherNode.replaceChild(newNode, v_node);
		
		v_node = childNodeCopy;
		return v_node;
	}

	function getTextNodes(parentNode)
	{
		var nodesList = new Array;
		var layer = parentNode.childNodes;
		var childNum = layer.length;

		for (var i = 0; i < childNum; i++)
		{
			var childNode = layer[i];
			var nodeType = childNode.nodeType;
			if (nodeType == 3 && childNode.data.trim().length>0) /* Text node */
			{
				nodesList[nodesList.length] = childNode;
			}
			if (nodeType == 1) /* Element node */
			{
				nodesList = nodesList.concat(getTextNodes(childNode));
			}
		}
		return nodesList;
	}
	