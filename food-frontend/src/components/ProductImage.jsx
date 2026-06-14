    import { useState } from 'react'

// Renders a product image, falling back to a clean gray placeholder box when
// the URL is missing or fails to load. Sizing comes from the parent container.
export default function ProductImage({ src, alt }) {
  const [failed, setFailed] = useState(false)

  if (!src || failed) {
    return <div className="product-img placeholder" role="img" aria-label="No image" />
  }

  return (
    <img
      className="product-img"
      src={src}
      alt={alt}
      loading="lazy"
      onError={() => setFailed(true)}
    />
  )
}
