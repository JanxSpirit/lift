/*
 * Copyright 2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package net.liftweb.http.rest

import net.liftweb._
import util._
import Helpers._

import scala.xml.{NodeSeq, Text, Elem, UnprefixedAttribute, Null}

/**
* Mix this trait into your REST service provider to convert between different
* response and a ResponseIt
*/
trait XMLApiHelper {
  implicit def boolToResponse(in: Boolean): ResponseIt =
  buildResponse(in, Empty, <xml:group/>)
  
  implicit def canBoolToResponse(in: Can[Boolean]): ResponseIt =
  buildResponse(in openOr false, in match {
      case Failure(msg, _, _) => Full(Text(msg))
      case _ => Empty
    }, <xml:group/>)

  implicit def pairToResponse(in: (Boolean, String)): ResponseIt =
  buildResponse(in._1, Full(Text(in._2)), <xml:group/>)
  
  implicit def unitToSuccess(in: Unit): ResponseIt = 
  buildResponse(true, Empty, <xml:group/>)
  
  protected def operation: Option[NodeSeq] =
  (for (req <- S.request) yield req.path.partPath match {
      case _ :: name :: _ => name
      case _ => ""
    }).map(Text)

  implicit def canNodeToResponse(in: Can[NodeSeq]): ResponseIt = in match {
    case Full(n) => buildResponse(true, Empty, n)
    case Failure(msg, _, _) => buildResponse(false, Full(Text(msg)), Text(""))
    case _ => buildResponse(false, Empty, Text(""))
  }
  
  implicit def putResponseInCan(in: ResponseIt): Can[ResponseIt] = Full(in)

  /**
  * The method that wraps the outer-most tag around the body
  */
  def createTag(in: NodeSeq): Elem

  /**
  * The attribute name for success
  */
  def successAttrName = "success"

  /**
  * The attribute name for operation
  */
  def operationAttrName = "operation"

  /**
  * The attribute name for any msg attribute
  */
  def msgAttrName = "msg"

  /**
  * Build the Response based on Success, an optional message
  * and the body
  */
  protected def buildResponse(success: Boolean, msg: Can[NodeSeq],
                            body: NodeSeq): ResponseIt =
  XmlResponse(createTag(body) % (successAttrName -> success) %
              (new UnprefixedAttribute(operationAttrName, operation, Null)) %
              (new UnprefixedAttribute(msgAttrName, msg, Null)))

  //<harpoon_api success={success.toString}
    //  msg={msg} operation={operation}>{body}</harpoon_api>)
}
