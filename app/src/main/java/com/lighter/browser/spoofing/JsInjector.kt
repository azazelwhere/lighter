package com.lighter.browser.spoofing

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

/**
 * Builds the JavaScript payload that overrides navigator/screen/WebGL/canvas/etc.
 * Injected via WebView.evaluateJavascript() on every page (onPageStarted) and
 * additionally via WebViewClient.onResourceRequest() for cross-origin iframes.
 *
 * All overrides are applied *before* the page's own scripts run, using a frozen
 * property descriptor so that detection scripts cannot simply re-assign.
 */
object JsInjector {

    fun buildScript(p: SpoofProfile): String {
        val json = JSONObject().apply {
            put("userAgent", p.userAgent)
            put("platform", p.platform)
            put("vendor", p.vendor)
            put("language", p.language)
            put("languages", JSONArray(p.languages))
            put("timezone", p.timezone)
            put("tzOffset", p.timezoneOffsetMinutes)
            put("screenWidth", p.screenWidth)
            put("screenHeight", p.screenHeight)
            put("dpr", p.devicePixelRatio)
            put("colorDepth", p.colorDepth)
            put("cores", p.hardwareConcurrency)
            put("memory", p.deviceMemory)
            put("webglVendor", p.webglVendor)
            put("webglRenderer", p.webglRenderer)
            put("canvasNoise", p.canvasNoise.toDouble())
            put("batteryLevel", p.batteryLevel.toDouble())
            put("batteryCharging", p.batteryCharging)
            put("blockBattery", p.blockBattery)
            put("blockSensors", p.blockSensors)
            put("fonts", JSONArray(p.fontsList))
            put("plugins", JSONArray(p.pluginsList))
            put("extraJs", p.extraJs)
        }.toString()

        // We pass the JSON config via base64 to avoid escaping headaches inside the IIFE.
        val b64 = Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP)
        return "(function(){var CONFIG=JSON.parse(atob('$b64'));\n" + CORE_JS + "\n})();"
    }

    /**
     * The actual override code. Kept as a Kotlin string to avoid asset lookups.
     */
    private val CORE_JS = """
        try {
          // ---- navigator.userAgent
          defineProp(navigator, 'userAgent', { get: function(){ return CONFIG.userAgent; } });
          defineProp(navigator, 'appVersion', { get: function(){
            return CONFIG.userAgent.replace(/^Mozilla\//,'');
          }});
          defineProp(navigator, 'platform', { get: function(){ return CONFIG.platform; } });
          defineProp(navigator, 'vendor', { get: function(){ return CONFIG.vendor; } });
          defineProp(navigator, 'language', { get: function(){ return CONFIG.language; } });
          defineProp(navigator, 'languages', { get: function(){ return CONFIG.languages; } });
          defineProp(navigator, 'hardwareConcurrency', { get: function(){ return CONFIG.cores; } });
          if ('deviceMemory' in navigator) {
            defineProp(navigator, 'deviceMemory', { get: function(){ return CONFIG.memory; } });
          }
          defineProp(navigator, 'maxTouchPoints', { get: function(){
            return /iPhone|Android/.test(CONFIG.platform) ? 5 : 0;
          }});

          // ---- plugins
          try {
            var fakePlugins = CONFIG.plugins.map(function(name, i){
              return { name: name, filename: name.toLowerCase().replace(/\\s/g,'')+'.pdf', description: name, length: 1 };
            });
            defineProp(navigator, 'plugins', { get: function(){
              var arr = fakePlugins.slice();
              arr.item = function(i){ return arr[i] || null; };
              arr.namedItem = function(n){ return arr.find(function(p){ return p.name===n; }) || null; };
              arr.refresh = function(){};
              return arr;
            }});
          } catch(e){}

          // ---- screen
          defineProp(screen, 'width', { get: function(){ return CONFIG.screenWidth; } });
          defineProp(screen, 'height', { get: function(){ return CONFIG.screenHeight; } });
          defineProp(screen, 'availWidth', { get: function(){ return CONFIG.screenWidth; } });
          defineProp(screen, 'availHeight', { get: function(){ return CONFIG.screenHeight - 40; } });
          defineProp(screen, 'colorDepth', { get: function(){ return CONFIG.colorDepth; } });
          defineProp(screen, 'pixelDepth', { get: function(){ return CONFIG.colorDepth; } });
          defineProp(window, 'devicePixelRatio', { get: function(){ return CONFIG.dpr; } });
          defineProp(window, 'outerWidth', { get: function(){ return CONFIG.screenWidth; } });
          defineProp(window, 'outerHeight', { get: function(){ return CONFIG.screenHeight; } });

          // ---- timezone
          try {
            var tz = CONFIG.timezone;
            var OriginalDateTimeFormat = Intl.DateTimeFormat;
            var spoofedFormat = function(){
              var args = arguments;
              if (args.length === 0) return new OriginalDateTimeFormat(tz);
              if (args.length === 1) return new OriginalDateTimeFormat(args[0], args[1]);
              if (args.length === 2) {
                var opts = args[1] || {};
                opts.timeZone = opts.timeZone || tz;
                return new OriginalDateTimeFormat(args[0], opts);
              }
              return new OriginalDateTimeFormat();
            };
            spoofedFormat.prototype = OriginalDateTimeFormat.prototype;
            Intl.DateTimeFormat = spoofedFormat;
            Intl.DateTimeFormat.supportedLocalesOf = OriginalDateTimeFormat.supportedLocalesOf;
          } catch(e){}

          // Date.prototype.getTimezoneOffset
          try {
            var offset = CONFIG.tzOffset;
            Date.prototype.getTimezoneOffset = function(){ return offset; };
          } catch(e){}

          // ---- WebGL fingerprint
          try {
            var origGetParameter = WebGLRenderingContext.prototype.getParameter;
            WebGLRenderingContext.prototype.getParameter = function(param){
              // UNMASKED_VENDOR_WEBGL = 0x9245
              if (param === 0x9245) return CONFIG.webglVendor;
              // UNMASKED_RENDERER_WEBGL = 0x9246
              if (param === 0x9246) return CONFIG.webglRenderer;
              // VENDOR = 0x1F00
              if (param === 0x1F00) return CONFIG.webglVendor;
              // RENDERER = 0x1F01
              if (param === 0x1F01) return CONFIG.webglRenderer;
              return origGetParameter.call(this, param);
            };
            if (window.WebGL2RenderingContext) {
              var origGetParam2 = WebGL2RenderingContext.prototype.getParameter;
              WebGL2RenderingContext.prototype.getParameter = function(param){
                if (param === 0x9245) return CONFIG.webglVendor;
                if (param === 0x9246) return CONFIG.webglRenderer;
                if (param === 0x1F00) return CONFIG.webglVendor;
                if (param === 0x1F01) return CONFIG.webglRenderer;
                return origGetParam2.call(this, param);
              };
            }
          } catch(e){}

          // ---- Canvas noise
          try {
            var noise = CONFIG.canvasNoise;
            if (noise > 0) {
              var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
              HTMLCanvasElement.prototype.toDataURL = function(){
                var ctx = this.getContext('2d');
                if (ctx && this.width > 0 && this.height > 0) {
                  try {
                    var img = ctx.getImageData(0, 0, Math.min(this.width, 16), Math.min(this.height, 16));
                    for (var i = 0; i < img.data.length; i += 4) {
                      // add tiny noise to RGB (skip alpha)
                      img.data[i]   = (img.data[i]   + Math.floor(Math.random()*2)) & 0xff;
                      img.data[i+1] = (img.data[i+1] + Math.floor(Math.random()*2)) & 0xff;
                      img.data[i+2] = (img.data[i+2] + Math.floor(Math.random()*2)) & 0xff;
                    }
                    ctx.putImageData(img, 0, 0);
                  } catch(e){} // CORS tainted canvas
                }
                return origToDataURL.apply(this, arguments);
              };

              var origToBlob = HTMLCanvasElement.prototype.toBlob;
              HTMLCanvasElement.prototype.toBlob = function(){
                var ctx = this.getContext('2d');
                if (ctx && this.width > 0 && this.height > 0) {
                  try {
                    var img = ctx.getImageData(0, 0, Math.min(this.width, 16), Math.min(this.height, 16));
                    for (var i = 0; i < img.data.length; i += 4) {
                      img.data[i]   = (img.data[i]   + Math.floor(Math.random()*2)) & 0xff;
                      img.data[i+1] = (img.data[i+1] + Math.floor(Math.random()*2)) & 0xff;
                      img.data[i+2] = (img.data[i+2] + Math.floor(Math.random()*2)) & 0xff;
                    }
                    ctx.putImageData(img, 0, 0);
                  } catch(e){}
                }
                return origToBlob.apply(this, arguments);
              };
            }
          } catch(e){}

          // ---- Battery API
          try {
            if (navigator.getBattery) {
              if (CONFIG.blockBattery) {
                navigator.getBattery = undefined;
              } else {
                var origGetBattery = navigator.getBattery.bind(navigator);
                navigator.getBattery = function(){
                  return origGetBattery().then(function(b){
                    var fakeLevel = CONFIG.batteryLevel;
                    var fakeCharging = CONFIG.batteryCharging;
                    try { defineProp(b, 'level', { get: function(){ return fakeLevel; } }); } catch(e){}
                    try { defineProp(b, 'charging', { get: function(){ return fakeCharging; } }); } catch(e){}
                    try { defineProp(b, 'chargingTime', { get: function(){ return fakeCharging ? 1800 : Infinity; } }); } catch(e){}
                    try { defineProp(b, 'dischargingTime', { get: function(){ return fakeCharging ? Infinity : 14400; } }); } catch(e){}
                    return b;
                  });
                };
              }
            }
          } catch(e){}

          // ---- Sensors
          try {
            if (CONFIG.blockSensors) {
              if (window.DeviceOrientationEvent) {
                window.DeviceOrientationEvent = function(){ throw new Error('blocked'); };
              }
              if (window.DeviceMotionEvent) {
                window.DeviceMotionEvent = function(){ throw new Error('blocked'); };
              }
              if (window.AmbientLightSensor) {
                window.AmbientLightSensor = function(){ throw new Error('blocked'); };
              }
              if (window.Gyroscope) window.Gyroscope = function(){ throw new Error('blocked'); };
              if (window.Accelerometer) window.Accelerometer = function(){ throw new Error('blocked'); };
              if (window.LinearAccelerationSensor) window.LinearAccelerationSensor = function(){ throw new Error('blocked'); };
              if (window.AbsoluteOrientationSensor) window.AbsoluteOrientationSensor = function(){ throw new Error('blocked'); };
              if (window.RelativeOrientationSensor) window.RelativeOrientationSensor = function(){ throw new Error('blocked'); };
              if (window.GravitySensor) window.GravitySensor = function(){ throw new Error('blocked'); };
            }
          } catch(e){}

          // ---- Fonts
          try {
            if (window.document && document.fonts && CONFIG.fonts.length) {
              // We cannot truly change installed fonts, but we can lie about check()
              var origCheck = document.fonts.check.bind(document.fonts);
              document.fonts.check = function(font, text){
                var family = (font || '').replace(/^\\d+\\w*\\s+/, '').replace(/['"]/g,'');
                if (CONFIG.fonts.indexOf(family) >= 0) return true;
                if (['Arial','sans-serif','serif','monospace','Times','Courier'].indexOf(family) >= 0) return true;
                return false;
              };
            }
          } catch(e){}

          // ---- Misc
          try { defineProp(navigator, 'connection', { get: function(){ return { effectiveType: '4g', rtt: 50, downlink: 10, saveData: false }; } }); } catch(e){}
          try { defineProp(navigator, 'doNotTrack', { get: function(){ return '1'; } }); } catch(e){}

          // ---- Extra user JS
          if (CONFIG.extraJs) {
            try { (0, eval)(CONFIG.extraJs); } catch(e){ console.warn('extraJs error:', e); }
          }
        } catch(err) { /* swallow */ }

        function defineProp(obj, prop, desc) {
          try {
            Object.defineProperty(obj, prop, desc);
          } catch(e) {
            try {
              // try on prototype
              var proto = Object.getPrototypeOf(obj);
              if (proto) Object.defineProperty(proto, prop, desc);
            } catch(e2){}
          }
        }
    """.trimIndent()
}
