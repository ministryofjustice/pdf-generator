This change is take from https://github.com/krokiet/flyingsaucer/commit/1242f5a6d61ebca30422235e2194a396102e6e8b which is a fork of flyingsaucer with a few fixes.


The problem solves an issue where `page-break-inside: avoid; not able to occur after sibling element that would only fit one line on a page and by mistake corrupt shared context.`

This has an impact that the final table in the document was being split over a page break.

Author said that this would one day be merged in to main project (see https://stackoverflow.com/questions/9499519/itext-2-flying-saucer-how-to-avoid-that-the-images-appears-broken-between-two) but that never appeared to happen.

For now we overwrite the jat version with this class (understanding the risk)

