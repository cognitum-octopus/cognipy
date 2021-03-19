/* jshint browser: true */

(function () {

// We'll copy the properties below into the mirror div.
// Note that some browsers, such as Firefox, do not concatenate properties
// into their shorthand (e.g. padding-top, padding-bottom etc. -> padding),
// so we have to list every single property explicitly.
var properties = [
  'direction',  // RTL support
  'boxSizing',
  'width',  // on Chrome and IE, exclude the scrollbar, so the mirror div wraps exactly as the textarea does
  'height',
  'overflowX',
  'overflowY',  // copy the scrollbar for IE

  'borderTopWidth',
  'borderRightWidth',
  'borderBottomWidth',
  'borderLeftWidth',
  'borderStyle',

  'paddingTop',
  'paddingRight',
  'paddingBottom',
  'paddingLeft',

  // https://developer.mozilla.org/en-US/docs/Web/CSS/font
  'fontStyle',
  'fontVariant',
  'fontWeight',
  'fontStretch',
  'fontSize',
  'fontSizeAdjust',
  'lineHeight',
  'fontFamily',

  'textAlign',
  'textTransform',
  'textIndent',
  'textDecoration',  // might not make a difference, but better be safe

  'letterSpacing',
  'wordSpacing',

  'tabSize',
  'MozTabSize'

];

var isBrowser = (typeof window !== 'undefined');
var isFirefox = (isBrowser && window.mozInnerScreenX != null);

function getCaretCoordinates(element, position, options) {
  if (!isBrowser) {
    throw new Error('textarea-caret-position#getCaretCoordinates should only be called in a browser');
  }

  var debug = options && options.debug || false;
  if (debug) {
    var el = document.querySelector('#input-textarea-caret-position-mirror-div');
    if (el) el.parentNode.removeChild(el);
  }

  // The mirror div will replicate the textarea's style
  var div = document.createElement('div');
  div.id = 'input-textarea-caret-position-mirror-div';
  document.body.appendChild(div);

  var style = div.style;
  var computed = window.getComputedStyle ? window.getComputedStyle(element) : element.currentStyle;  // currentStyle for IE < 9
  var isInput = element.nodeName === 'INPUT';

  // Default textarea styles
  style.whiteSpace = 'pre-wrap';
  if (!isInput)
    style.wordWrap = 'break-word';  // only for textarea-s

  // Position off-screen
  style.position = 'absolute';  // required to return coordinates properly
  if (!debug)
    style.visibility = 'hidden';  // not 'display: none' because we want rendering

  // Transfer the element's properties to the div
  properties.forEach(function (prop) {
    if (isInput && prop === 'lineHeight') {
      // Special case for <input>s because text is rendered centered and line height may be != height
      if (computed.boxSizing === "border-box") {
        var height = parseInt(computed.height);
        var outerHeight =
          parseInt(computed.paddingTop) +
          parseInt(computed.paddingBottom) +
          parseInt(computed.borderTopWidth) +
          parseInt(computed.borderBottomWidth);
        var targetHeight = outerHeight + parseInt(computed.lineHeight);
        if (height > targetHeight) {
          style.lineHeight = height - outerHeight + "px";
        } else if (height === targetHeight) {
          style.lineHeight = computed.lineHeight;
        } else {
          style.lineHeight = 0;
        }
      } else {
        style.lineHeight = computed.height;
      }
    } else {
      style[prop] = computed[prop];
    }
  });

  if (isFirefox) {
    // Firefox lies about the overflow property for textareas: https://bugzilla.mozilla.org/show_bug.cgi?id=984275
    if (element.scrollHeight > parseInt(computed.height))
      style.overflowY = 'scroll';
  } else {
    style.overflow = 'hidden';  // for Chrome to not render a scrollbar; IE keeps overflowY = 'scroll'
  }

  div.textContent = element.value.substring(0, position);
  // The second special handling for input type="text" vs textarea:
  // spaces need to be replaced with non-breaking spaces - http://stackoverflow.com/a/13402035/1269037
  if (isInput)
    div.textContent = div.textContent.replace(/\s/g, '\u00a0');

  var span = document.createElement('span');
  // Wrapping must be replicated *exactly*, including when a long word gets
  // onto the next line, with whitespace at the end of the line before (#7).
  // The  *only* reliable way to do that is to copy the *entire* rest of the
  // textarea's content into the <span> created at the caret position.
  // For inputs, just '.' would be enough, but no need to bother.
  span.textContent = element.value.substring(position) || '.';  // || because a completely empty faux span doesn't render at all
  div.appendChild(span);

  var coordinates = {
    top: span.offsetTop + parseInt(computed['borderTopWidth']),
    left: span.offsetLeft + parseInt(computed['borderLeftWidth']),
    height: parseInt(computed['lineHeight'])
  };

  if (debug) {
    span.style.backgroundColor = '#aaa';
  } else {
    document.body.removeChild(div);
  }

  return coordinates;
}

if (typeof module != 'undefined' && typeof module.exports != 'undefined') {
  module.exports = getCaretCoordinates;
} else if(isBrowser) {
  window.getCaretCoordinates = getCaretCoordinates;
}

}());

require.undef('ontoedit');

define('ontoedit', ["@jupyter-widgets/base"], function(widgets) {

    var OntoeditModel = widgets.TextareaModel.extend({

        defaults: $.extend(widgets.TextareaModel.prototype.defaults(), {
            _model_name: "OntoeditModel",
            _view_name: "OntoeditView",
            _model_module: "ontoedit",
            _view_module: "ontoedit",
            rows: null,
            continuous_update: true,
            cursor: 0,
            dot:0,
            hints:'',
            hintsX:0,
            hintT:''
        })
    });

    var OntoeditView = widgets.TextareaView.extend({

        render: function() {
            OntoeditView.__super__.render.apply(this, arguments);
            this.model.on('change:hints', this.hints_changed, this);
            this.model.on('change:hintsX', this.hintsX_changed, this);

            var model = this.model;
            var that = this;

            var last_position = 0;

            function cursor_changed(element) {
                var new_position = getCursorPosition(element);
                if (new_position !== last_position) {
                    last_position = new_position;
                    return true;
                }
                return false;
            }

            function getCursorPosition(element) {
                var el = $(element).get(0);
                var pos = 0;
                if ('selectionStart' in el) {
                    pos = el.selectionStart;
                } else if ('selection' in document) {
                    el.focus();
                    var Sel = document.selection.createRange();
                    var SelLength = document.selection.createRange().text.length;
                    Sel.moveStart('character', -el.value.length);
                    pos = Sel.text.length - SelLength;
                }
                return pos;
            }

            var oldVal = "";

            this.dd=$('<div style="-moz-appearance: textfield-multiline;-webkit-appearance: textarea;font: medium -moz-fixed; font: -webkit-small-control; position: absolute;z-index: 1; background-color:navy;color:white;box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.4); "/>')

            $(this.el).bind("click keyup focus",function(e){
                if (that.dd.get(0).style.visibility!="visible")
                    return;
                
                var ele=$($(that.el).children("textarea")[0])

                if(cursor_changed(ele)){
                   var  cursor = getCursorPosition(ele);
//                    console.log(cursor);
//                    console.log('(top, left, height) = (%s, %s, %s)', caret.top, caret.left, caret.height);
                    model.set('cursor',cursor);
//                    console.log(cursor)
                    that.touch();
                }
            });

            $($(this.el).children("textarea")[0]).bind("blur",function(e){
//                console.log("blur")
                  model.set('dot',model.get('dot')+1);
                  that.touch();
                that.dd.get(0).style.visibility="hidden";
            });

            $($(this.el).children("textarea")[0]).bind("keydown",function(e){
                  if (String.fromCharCode(event.which)=='\t')
                  {
                        if (that.dd.get(0).style.visibility!="visible")
                        {
                            var ele=$($(that.el).children("textarea")[0])

                            if(cursor_changed(ele)){
                               var  cursor = getCursorPosition(ele);
            //                    console.log(cursor);
            //                    console.log('(top, left, height) = (%s, %s, %s)', caret.top, caret.left, caret.height);
                                model.set('cursor',cursor);
            //                    console.log(cursor)
                                that.touch();
                                that.dd.get(0).style.visibility="visible";
                            }
                        }
//                      console.log(model.get('hintT'));
                      var target=$($(that.el).children("textarea")[0])
                      var curpos = getCursorPosition(target)
                      var off=curpos-model.get('hintsX')
                      var data = model.get('hintT').slice(off)
//                      console.log(off)

                      if (target.setRangeText) {
                         //if setRangeText function is supported by current browser
                         target.setRangeText(data)
                      } else {
                        target.focus()
                        document.execCommand('insertText', false /*no UI*/, data);
                      }
                      e.preventDefault();
                      return;
                  }
                  else if (event.key=='.')
                  {
                      model.set('dot',model.get('dot')+1);
                      that.touch();
                  }
                  else if(event.key== "Escape")
                  {
                    that.dd.get(0).style.visibility="hidden";
                  }
            });

            // this.el represents the widget's DOM
            $(this.el)
            .append(this.dd)
        },

        hints_changed: function() {
            var that = this;
            var model = this.model;
            that.dd.get(0).innerHTML=model.get('hints')
//            console.log(model.get('hints'))
        },
        hintsX_changed: function() {
            var that = this;
            var model = this.model;
            var ele=$($(that.el).children("textarea")[0]);
            element=ele.get(0);
            var caret = getCaretCoordinates(ele.get(0), model.get('hintsX'));
            that.dd.get(0).style.top = element.offsetTop - element.scrollTop + caret.top+ caret.height-5 + 'px';
            that.dd.get(0).style.left = element.offsetLeft - element.scrollLeft + caret.left + 'px';
//            that.dd.get(0).style.width = '20%';
//            that.dd.get(0).style.height = '50%';
            that.dd.get(0).style.fontFamily=element.style.fontFamily;
            that.dd.get(0).style.fontSize=element.style.fontSize;
//                        console.log(model.get('hintsX'))
        },
    });

    return {
        OntoeditModel : OntoeditModel,
        OntoeditView : OntoeditView
    };
});
