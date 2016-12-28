<script>
    $('html').on('dragover', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $(this).addClass('dragging');
    });

    $('html').on('dragleave', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $(this).removeClass('dragging');
    });

    $('html').on('drop', function(e) {
        e.preventDefault();
        e.stopPropagation();
        loadImage(e.originalEvent.dataTransfer.files[0]);
    });

    function loadImage(file) {
        var imageType = /image.*/;
        if (!file.type.match(imageType)) {
          return;
        }
        var reader = new FileReader();
        reader.onload = (function(x) {
            return function(e) {
                x.src = e.target.result;
            };
        })(img);
        reader.readAsDataURL(file);
    }

    function start() {
        running = true;
        updateView();
        updateModel();
    }

    function stop() {
        running = false;
    }

    function reset() {
        var width = 1024;
        var height = 1024;
        svg.attr("viewBox", "0 0 " + width + " " + height);
        canvas.width = width;
        canvas.height = height;
        context.drawImage(img, 0, 0, width, height);
        quads = [];
        quads.push(createQuad(0, 0, width, height, "#000000"));
        redraw();
    }

    var leaf_size = 4;
    var area_power = 0.25;

    var canvas = document.getElementById("source");
    var context = canvas.getContext("2d");
    var svg = d3.select("svg");
    var quads = [];
    var id = 0;
    var updateViewTimer = null;
    var updateModelTimer = null;
    var running = true;

    var img = new Image();
    img.src = "owl.jpg";
    img.onload = function() {
        reset();
        updateView();
        updateModel();
    };

    function key(d) {
        return d.id;
    }

    function step() {
        var items = _.filter(quads, function(x) {
            return !x.leaf;
        });
        var quad = _.min(items, function(x) {
            return x.score;
        });
        split(quad);
    }

    function updateModel() {
        clearTimeout(updateModelTimer);
        if (running) {
            step();
            updateModelTimer = setTimeout(updateModel, 1);
        }
    }

    function updateView() {
        clearTimeout(updateViewTimer);
        if (running) {
            redraw();
            updateViewTimer = setTimeout(updateView, 200);
        }
    }

    function redraw(highlight) {
        var iterations = (quads.length - 1) / 3;
        d3.select("#info")
            .text('Iterations: ' + iterations + ' - Shapes: ' + quads.length);
        var rect = svg.selectAll("rect").data(quads, key);
        rect.exit()
            .remove();
        rect.enter()
            .append("rect")
            .attr("x", function(d) { return d.x + 0.5; })
            .attr("y", function(d) { return d.y + 0.5; })
            .attr("width", function(d) { return d.w - 0.25; })
            .attr("height", function(d) { return d.h - 0.25; })
            .attr("fill", function(d) { return highlight ? '#ffffff' : d.previousFill; })
            .on("click", function(d) {
                split(d);
                redraw();
            })
            .transition()
            .duration(200)
            .styleTween("fill", function(d) {
                return d3.interpolate(
                    highlight ? '#ffffff' : d.previousFill, d.fill);
            })
            ;
    }

    function split(quad) {
        if (quad.leaf) {
            return;
        }
        var index = quads.indexOf(quad);
        quads.splice(index, 1);
        var w = quad.w / 2;
        var h = quad.h / 2;
        var x1 = quad.x;
        var x2 = quad.x + w;
        var y1 = quad.y;
        var y2 = quad.y + h;
        quads.push(createQuad(x1, y1, w, h, quad.fill));
        quads.push(createQuad(x2, y1, w, h, quad.fill));
        quads.push(createQuad(x1, y2, w, h, quad.fill));
        quads.push(createQuad(x2, y2, w, h, quad.fill));
    }

    function createQuad(x, y, w, h, previousFill) {
        id++;
        var c = computeColor(x, y, w, h);
        var error = c[3];
        var score = -error * Math.pow(w * h, area_power);
        var color = 16777216 + (c[0] << 16) + (c[1] << 8) + c[2];
        var fill = '#' + color.toString(16).substring(1);
        var leaf = w <= leaf_size || h <= leaf_size;
        return {
            id: id,
            x: x, y: y, w: w, h: h,
            fill: fill, leaf: leaf, score: score,
            previousFill: previousFill
        }
    }

    function computeHistogram(x, y, w, h) {
        var data = context.getImageData(x, y, w, h).data;
        var result = [];
        for (var i = 0; i < 1024; i++) {
            result.push(0);
        }
        for (var i = 0; i < data.length; i++) {
            result[(i % 4) * 256 + data[i]]++;
        }
        return result;
    }

    function weightedAverage(hist) {
        var total = 0;
        var value = 0;
        for (var i = 0; i < 256; i++) {
            total += hist[i];
            value += hist[i] * i;
        }
        value = value / total;
        var error = 0;
        for (var i = 0; i < 256; i++) {
            error += (value - i) * (value - i) * hist[i];
        }
        error = Math.sqrt(error / total);
        return [value, error];
    }

    function colorFromHistogram(hist) {
        var c1 = weightedAverage(hist.slice(0, 256));
        var c2 = weightedAverage(hist.slice(256, 512));
        var c3 = weightedAverage(hist.slice(512, 768));
        var r = Math.round(c1[0]);
        var g = Math.round(c2[0]);
        var b = Math.round(c3[0]);
        var e = c1[1] * 0.2989 + c2[1] * 0.5870 + c3[1] * 0.1140;
        return [r, g, b, e];
    }

    function computeColor(x, y, w, h) {
        return colorFromHistogram(computeHistogram(x, y, w, h));
    }
</script>