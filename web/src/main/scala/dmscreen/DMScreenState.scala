/*
 * Copyright (c) 2024 Roberto Leibman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dmscreen

import dmscreen.dnd5e.*
import japgolly.scalajs.react.React.Context
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.{Callback, React}
import org.scalajs.dom.window

trait CampaignState {

  def gameUI:         GameUI
  def campaignHeader: CampaignHeader
  def saveChanges():  AsyncCallback[CampaignState]
  def loadChanges():  AsyncCallback[CampaignState]

}

enum DialogMode {

  case open, closed

}

case class DMScreenState(
  user:             Option[User] = None,
  campaignState:    Option[CampaignState] = None,
  onSelectCampaign: Option[CampaignHeader] => Callback = _ => Callback.empty,
  onModifyCampaignState: (CampaignState, String) => Callback = (
    _,
    _
  ) => Callback.empty,
  dialogMode:       DialogMode = DialogMode.closed,
  changeDialogMode: DialogMode => Callback = _ => Callback.empty,
  onForceSave:      Callback = Callback.empty
  //  operationStream: Option[WebSocketHandler] = None
) {

  def log(str: String): Callback = {
    campaignState.fold(Callback.empty)(s => onModifyCampaignState(s, str))
  }

}

object DMScreenState {

  val ctx: Context[DMScreenState] = React.createContext(DMScreenState())

}
