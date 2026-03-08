import React, { useState } from 'react';
import { Light as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vs2015 } from 'react-syntax-highlighter/dist/esm/styles/hljs';
import { CodeGeneratorService, GeneratedCode } from '../services/CodeGeneratorService';

export const Generator: React.FC = () => {
  const [storyKey, setStoryKey] = useState('');
  const [designFile, setDesignFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [generatedCode, setGeneratedCode] = useState<GeneratedCode | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'react' | 'spring'>('react');

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] ?? null;
    setDesignFile(file);
    if (file) {
      const url = URL.createObjectURL(file);
      setPreviewUrl(url);
    } else {
      setPreviewUrl(null);
    }
  };

  const handleGenerate = async () => {
    if (!storyKey || !designFile) {
      setError('Please provide both a Jira story key and a design file.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const service = new CodeGeneratorService();
      const result = await service.generateApplication(storyKey, designFile);
      setGeneratedCode(result);
    } catch (err) {
      setError('Failed to generate code. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const downloadCode = (content: string, filename: string) => {
    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">AI Full‑Stack Generator</h1>
          <p className="text-gray-600 mt-2">Turn a Jira story + Figma design into code</p>
        </div>

        {/* Input Card */}
        <div className="bg-white rounded-xl shadow-md p-6 mb-8">
          <div className="space-y-4">
            {/* Jira Story Input */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Jira Story Key
              </label>
              <input
                type="text"
                value={storyKey}
                onChange={(e) => setStoryKey(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                placeholder="PROJECT-123"
              />
            </div>

            {/* File Upload */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Figma Design Screenshot
              </label>
              <div className="flex items-center justify-center w-full">
                <label className="flex flex-col w-full h-32 border-2 border-gray-300 border-dashed rounded-lg cursor-pointer hover:bg-gray-50">
                  <input type="file" accept="image/*" className="hidden" onChange={handleFileChange} />
                </label>
              </div>
              {previewUrl && (
                <div className="mt-2 relative inline-block">
                  <img src={previewUrl} alt="Preview" className="h-20 rounded border" />
                  <button
                    onClick={() => {
                      setDesignFile(null);
                      setPreviewUrl(null);
                    }}
                    className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs"
                  >
                    ×
                  </button>
                </div>
              )}
            </div>

            {/* Generate Button */}
            <button
              onClick={handleGenerate}
              disabled={loading}
              className={`w-full py-2 px-4 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ${
                loading ? 'opacity-50 cursor-not-allowed' : ''
              }`}
            >
              {loading ? 'Generating...' : 'Generate Code'}
            </button>

            {error && <div className="text-red-600 text-sm">{error}</div>}
          </div>
        </div>

        {/* Generated Code Preview */}
        {generatedCode && (
          <div className="bg-white rounded-xl shadow-md p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-800">Generated Code</h2>
              <div className="flex items-center space-x-2">
                {/* Tab Switcher */}
                <div className="flex space-x-2">
                  <button
                    onClick={() => setActiveTab('react')}
                    className={`px-3 py-1 text-sm rounded-md ${
                      activeTab === 'react' ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-600'
                    }`}
                  >
                    React
                  </button>
                  <button
                    onClick={() => setActiveTab('spring')}
                    className={`px-3 py-1 text-sm rounded-md ${
                      activeTab === 'spring' ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-600'
                    }`}
                  >
                    Spring Boot
                  </button>
                </div>
                {/* Download Button */}
                <button
                  onClick={() => downloadCode(
                    activeTab === 'react' ? generatedCode.reactComponents : generatedCode.springEntities,
                    activeTab === 'react' ? 'Component.tsx' : 'Entity.java'
                  )}
                  className="ml-2 px-3 py-1 text-sm bg-green-600 text-white rounded-md hover:bg-green-700"
                >
                  Download
                </button>
              </div>
            </div>
            <div className="bg-gray-900 rounded-md overflow-auto max-h-96">
              <SyntaxHighlighter
                language={activeTab === 'react' ? 'typescript' : 'java'}
                style={vs2015}
                showLineNumbers
                customStyle={{ margin: 0 }}
              >
                {activeTab === 'react' ? generatedCode.reactComponents : generatedCode.springEntities}
              </SyntaxHighlighter>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};